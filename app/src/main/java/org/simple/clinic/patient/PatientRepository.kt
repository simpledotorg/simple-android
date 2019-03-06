package org.simple.clinic.patient

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.AppDatabase
import org.simple.clinic.analytics.OperationTimingTracker
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.patient.filter.SearchPatientByName
import org.simple.clinic.patient.recent.RecentPatient
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Arrays
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

typealias PatientUuid = UUID
typealias FacilityUuid = UUID

@AppScope
class PatientRepository @Inject constructor(
    private val database: AppDatabase,
    private val dobValidator: UserInputDateValidator,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val numberValidator: PhoneNumberValidator,
    private val utcClock: UtcClock,
    private val searchPatientByName: SearchPatientByName,
    private val configProvider: Observable<PatientConfig>,
    private val reportsRepository: ReportsRepository,
    @Named("date_for_user_input") private val dateOfBirthFormat: DateTimeFormatter
) : SynceableRepository<PatientProfile, PatientPayload> {

  private var ongoingNewPatientEntry: OngoingNewPatientEntry = OngoingNewPatientEntry()

  fun search(name: String): Observable<List<PatientSearchResult>> {
    val timingTracker = OperationTimingTracker("Search Patient", utcClock)

    val fetchPatientNameAnalytics = "Fetch Name and Id"
    val fuzzyFilterPatientNameAnalytics = "Fuzzy Filtering By Name"
    val fetchPatientDetailsAnalytics = "Fetch Patient Details"
    val sortByVisitedFacilityAnalytics = "Sort By Visited Facility"

    val patientUuidsMatchingName = database.patientSearchDao()
        .nameAndId(PatientStatus.ACTIVE)
        .toObservable()
        .switchMapSingle {
          timingTracker.stop(fetchPatientNameAnalytics)
          timingTracker.start(fuzzyFilterPatientNameAnalytics)
          searchPatientByName.search(name, it)
              .doOnSuccess { timingTracker.stop(fuzzyFilterPatientNameAnalytics) }
        }

    return Observables.combineLatest(patientUuidsMatchingName, configProvider)
        .map { (uuids, config) -> uuids.take(config.limitOfSearchResults) }
        .switchMapSingle { matchingUuidsSortedByScore ->
          when {
            matchingUuidsSortedByScore.isEmpty() -> Single.just(emptyList())
            else -> {
              database.patientSearchDao()
                  .searchByIds(matchingUuidsSortedByScore, PatientStatus.ACTIVE)
                  .doOnSubscribe { timingTracker.start(fetchPatientDetailsAnalytics) }
                  .map { results ->
                    timingTracker.stop(fetchPatientDetailsAnalytics)
                    val resultsByUuid = results.associateBy { it.uuid }
                    matchingUuidsSortedByScore.map { resultsByUuid[it]!! }
                  }
            }
          }
        }
        .doOnNext { timingTracker.start(sortByVisitedFacilityAnalytics) }
        .compose(sortByCurrentFacility())
        .doOnSubscribe { timingTracker.start(fetchPatientNameAnalytics) }
        .doOnNext { timingTracker.stop(sortByVisitedFacilityAnalytics) }
  }

  // TODO: Get user from caller.
  private fun sortByCurrentFacility(): ObservableTransformer<List<PatientSearchResult>, List<PatientSearchResult>> {
    return ObservableTransformer { upstream ->
      val searchResults = upstream.replay().refCount()

      val currentFacilityUuidStream = userSession
          .requireLoggedInUser()
          .switchMap { facilityRepository.currentFacility(it) }
          .map { it.uuid }

      val patientToFacilityUuidStream = searchResults
          .map { patients -> patients.map { it.uuid } }
          .switchMap {
            database
                .bloodPressureDao()
                .patientToFacilityIds(it)
                .toObservable()
          }

      Observables.combineLatest(searchResults, currentFacilityUuidStream, patientToFacilityUuidStream)
          .map { (patients, currentFacility, patientToFacilities) ->
            val patientToUniqueFacilities = patientToFacilities
                .fold(mutableMapOf<PatientUuid, MutableSet<FacilityUuid>>()) { facilityUuids, (patientUuid, facilityUuid) ->
                  if (patientUuid !in facilityUuids) {
                    facilityUuids[patientUuid] = mutableSetOf()
                  }

                  facilityUuids[patientUuid]?.add(facilityUuid)
                  facilityUuids
                } as Map<PatientUuid, Set<FacilityUuid>>

            patients.sortedByDescending {
              patientToUniqueFacilities[it.uuid]?.contains(currentFacility) ?: false
            }
          }
    }
  }

  private fun savePatient(patient: Patient): Completable = Completable.fromAction { database.patientDao().save(patient) }

  fun patient(uuid: UUID): Observable<Optional<Patient>> {
    return database.patientDao()
        .patient(uuid)
        .toObservable()
        .map { patients ->
          when {
            patients.isNotEmpty() -> Just(patients.first())
            else -> None
          }
        }
  }

  fun updatePatientStatusToDead(patientUuid: UUID): Completable {
    return Completable.fromAction {
      database
          .patientDao()
          .updatePatientStatus(
              uuid = patientUuid,
              newStatus = PatientStatus.DEAD,
              newSyncStatus = SyncStatus.PENDING,
              newUpdatedAt = Instant.now(utcClock))
    }
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): Single<List<PatientProfile>> {
    return database.patientDao()
        .recordsWithSyncStatus(syncStatus)
        .firstOrError()
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus): Completable {
    return Completable.fromAction { database.patientDao().updateSyncStatus(from, to) }
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus): Completable {
    return Completable.fromAction { database.patientDao().updateSyncStatus(ids, to) }
  }

  override fun recordCount(): Observable<Int> {
    return database.patientDao()
        .patientCount()
        .toObservable()
  }

  override fun mergeWithLocalData(payloads: List<PatientPayload>): Completable {
    return Single.fromCallable {
      payloads.asSequence()
          .filter { payload ->
            database.patientDao().getOne(payload.uuid)?.syncStatus.canBeOverriddenByServerCopy()
          }
          .map(::payloadToPatientProfile)
          .toList()
    }.flatMapCompletable(::save)
  }

  override fun save(records: List<PatientProfile>): Completable {
    return Completable.fromAction {
      database
          .addressDao()
          .save(records.map { it.address })

      database
          .patientDao()
          .save(records.map { it.patient })

      database
          .phoneNumberDao()
          .save(records
              .filter { it.phoneNumbers.isNotEmpty() }
              .flatMap { it.phoneNumbers })

      database
          .businessIdDao()
          .save(records
              .filter { it.businessIds.isNotEmpty() }
              .flatMap { it.businessIds })
    }
  }

  private fun payloadToPatientProfile(patientPayload: PatientPayload): PatientProfile {
    val patient = patientPayload.toDatabaseModel(newStatus = DONE)

    val patientAddress = patientPayload.address.toDatabaseModel()

    val phoneNumbers = patientPayload
        .phoneNumbers
        ?.map { it.toDatabaseModel(patientPayload.uuid) } ?: emptyList()

    val businessIds = patientPayload
        .businessIds
        ?.map { it.toDatabaseModel() } ?: emptyList()

    return PatientProfile(
        patient = patient,
        address = patientAddress,
        phoneNumbers = phoneNumbers,
        businessIds = businessIds
    )
  }

  fun ongoingEntry(): Single<OngoingNewPatientEntry> {
    return Single.fromCallable { ongoingNewPatientEntry }
  }

  fun saveOngoingEntry(ongoingEntry: OngoingNewPatientEntry): Completable {
    return Completable.fromAction {
      this.ongoingNewPatientEntry = ongoingEntry
    }
  }

  fun saveOngoingEntryAsPatient(): Single<Patient> {
    val cachedOngoingEntry = ongoingEntry().cache()

    val validation = cachedOngoingEntry
        .flatMapCompletable {
          val validationErrors = it.validationErrors(dobValidator, numberValidator)
          if (validationErrors.isEmpty()) {
            Completable.complete()
          } else {
            val errorNames = validationErrors.map { it.name }.toTypedArray()
            Completable.error(AssertionError("Patient entry has errors: ${Arrays.toString(errorNames)}"))
          }
        }

    val addressUuid = UUID.randomUUID()
    val addressSave = cachedOngoingEntry
        .map {
          with(it) {
            PatientAddress(
                uuid = addressUuid,
                colonyOrVillage = address!!.colonyOrVillage,
                district = address.district,
                state = address.state,
                createdAt = Instant.now(utcClock),
                updatedAt = Instant.now(utcClock),
                deletedAt = null)
          }
        }
        .flatMapCompletable { address -> saveAddress(address) }

    val patientUuid = UUID.randomUUID()
    val sharedPatient = cachedOngoingEntry
        .map {
          with(it) {
            Patient(
                uuid = patientUuid,
                addressUuid = addressUuid,
                fullName = personalDetails!!.fullName,
                searchableName = nameToSearchableForm(personalDetails.fullName),
                gender = personalDetails.gender!!,

                dateOfBirth = convertToDate(personalDetails.dateOfBirth),
                age = personalDetails.age?.let {
                  Age(value = personalDetails.age.toInt(),
                      updatedAt = Instant.now(utcClock),
                      computedDateOfBirth = LocalDate.now(utcClock).minusYears(personalDetails.age.toLong()))
                },

                status = PatientStatus.ACTIVE,

                createdAt = Instant.now(utcClock),
                updatedAt = Instant.now(utcClock),
                deletedAt = null,
                syncStatus = PENDING)
          }
        }
        .cache()

    val patientSave = sharedPatient
        .flatMapCompletable { patient -> savePatient(patient) }

    val phoneNumberSave = cachedOngoingEntry
        .flatMapCompletable { entry ->
          if (entry.phoneNumber == null) {
            Completable.complete()
          } else {
            val number = with(entry.phoneNumber) {
              PatientPhoneNumber(
                  uuid = UUID.randomUUID(),
                  patientUuid = patientUuid,
                  number = number,
                  phoneType = type,
                  active = active,
                  createdAt = Instant.now(utcClock),
                  updatedAt = Instant.now(utcClock),
                  deletedAt = null)
            }
            savePhoneNumber(number)
          }
        }

    return validation
        .andThen(addressSave)
        .andThen(patientSave)
        .andThen(phoneNumberSave)
        .andThen(sharedPatient)
  }

  fun updatePatient(patient: Patient): Completable {
    return Completable.fromAction {
      val patientToSave = patient.copy(
          searchableName = nameToSearchableForm(patient.fullName),
          updatedAt = Instant.now(utcClock),
          syncStatus = PENDING
      )
      database.patientDao().save(patientToSave)
    }
  }

  fun updateAddressForPatient(patientUuid: UUID, patientAddress: PatientAddress): Completable {
    return Completable
        .fromAction {
          val updatedPatientAddress = patientAddress.copy(updatedAt = Instant.now(utcClock))
          database.addressDao().save(updatedPatientAddress)
        }
        .andThen(Completable.fromAction {
          database.patientDao().updateSyncStatus(listOf(patientUuid), PENDING)
        })
  }

  fun updatePhoneNumberForPatient(patientUuid: UUID, phoneNumber: PatientPhoneNumber): Completable {
    return savePhoneNumber(phoneNumber.copy(updatedAt = Instant.now(utcClock)))
        .andThen(Completable.fromAction {
          database.patientDao().updateSyncStatus(listOf(patientUuid), PENDING)
        })
  }

  fun createPhoneNumberForPatient(patientUuid: UUID, number: String, phoneNumberType: PatientPhoneNumberType, active: Boolean): Completable {
    return Single
        .fromCallable {
          val now = Instant.now(utcClock)

          PatientPhoneNumber(
              uuid = UUID.randomUUID(),
              patientUuid = patientUuid,
              number = number,
              phoneType = phoneNumberType,
              active = active,
              createdAt = now,
              updatedAt = now,
              deletedAt = null
          )
        }
        .flatMapCompletable(this::savePhoneNumber)
        .andThen(Completable.fromAction {
          database.patientDao().updateSyncStatus(listOf(patientUuid), PENDING)
        })
  }

  private fun convertToDate(dateOfBirth: String?): LocalDate? {
    return dateOfBirth?.let { dateOfBirthFormat.parse(dateOfBirth, LocalDate::from) }
  }

  private fun saveAddress(address: PatientAddress): Completable {
    return Completable.fromAction {
      database.addressDao().save(address)
    }
  }

  fun address(addressUuid: UUID): Observable<Optional<PatientAddress>> {
    return database.addressDao()
        .address(addressUuid)
        .toObservable()
        .map { addresses ->
          when {
            addresses.isNotEmpty() -> Just(addresses.first())
            else -> None
          }
        }
  }

  private fun savePhoneNumber(number: PatientPhoneNumber): Completable {
    return Completable.fromAction {
      database.phoneNumberDao().save(listOf(number))
    }
  }

  fun phoneNumber(patientUuid: UUID): Observable<Optional<PatientPhoneNumber>> {
    return database.phoneNumberDao()
        .phoneNumber(patientUuid)
        .toObservable()
        .map { numbers ->
          when {
            numbers.isNotEmpty() -> Just(numbers.first())
            else -> None
          }
        }
  }

  fun clearPatientData(): Completable {
    return Completable
        .fromCallable {
          database.runInTransaction {
            database.patientDao().clear()
            database.phoneNumberDao().clear()
            database.addressDao().clear()
            database.bloodPressureDao().clearData()
            database.prescriptionDao().clearData()
            database.appointmentDao().clear()
            database.communicationDao().clear()
            database.medicalHistoryDao().clear()
          }
        }
        .andThen(reportsRepository.deleteReportsFile().toCompletable())
  }

  fun recentPatients(facilityUuid: UUID, limit: Int): Observable<List<RecentPatient>> =
      database.recentPatientDao()
          .recentPatients(facilityUuid, limit)
          .toObservable()
}
