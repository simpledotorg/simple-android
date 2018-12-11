package org.simple.clinic.patient

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.patient.filter.SearchPatientByName
import org.simple.clinic.patient.fuzzy.AgeFuzzer
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator
import org.threeten.bp.Clock
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
    private val dobValidator: DateOfBirthFormatValidator,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val numberValidator: PhoneNumberValidator,
    private val clock: Clock,
    private val ageFuzzer: AgeFuzzer,
    @Named("long_date") private val dateOfBirthFormat: DateTimeFormatter,
    private val searchPatientByName: SearchPatientByName,
    private val configProvider: Single<PatientConfig>
) : SynceableRepository<PatientProfile, PatientPayload> {

  private var ongoingNewPatientEntry: OngoingNewPatientEntry = OngoingNewPatientEntry()

  fun search(name: String, assumedAge: Int): Observable<List<PatientSearchResult>> {
    val ageBounds = ageFuzzer.bounded(assumedAge)
    val dateOfBirthLowerBound = ageBounds.upper.toString()
    val dateOfBirthUpperBound = ageBounds.lower.toString()

    return configProvider.flatMapObservable { (isFuzzySearchV2Enabled) ->
      if (isFuzzySearchV2Enabled) {
        searchV2(name = name, dateOfBirthUpperBound = dateOfBirthUpperBound, dateOfBirthLowerBound = dateOfBirthLowerBound)
      } else {
        searchV1(name = name, dateOfBirthUpperBound = dateOfBirthUpperBound, dateOfBirthLowerBound = dateOfBirthLowerBound)
      }
    }
  }

  @Deprecated(message = "replaced by search v2")
  private fun searchV1(name: String, dateOfBirthUpperBound: String, dateOfBirthLowerBound: String): Observable<List<PatientSearchResult>> {
    val searchableName = nameToSearchableForm(name)

    val substringSearch = database.patientSearchDao()
        .search(searchableName, dateOfBirthUpperBound, dateOfBirthLowerBound, PatientStatus.ACTIVE)
        .toObservable()

    val fuzzySearch = database.fuzzyPatientSearchDao()
        .searchForPatientsWithNameLikeAndAgeWithin(searchableName, dateOfBirthUpperBound, dateOfBirthLowerBound)
        .toObservable()

    return Observables.zip(substringSearch, fuzzySearch, configProvider.toObservable())
        .map { (results, fuzzyResults, config) ->
          // Fuzzy search has an internal limit, but the substring search also can return more than
          // a thousand results which can fail when we sort on the current facility since it
          // queries the blood pressure table with an IN clause which can cause an exception.
          (fuzzyResults + results).distinctBy { it.uuid }.take(config.limitOfSearchResults)
        }
        .compose(sortByCurrentFacility())
  }

  private fun searchV2(name: String, dateOfBirthUpperBound: String, dateOfBirthLowerBound: String): Observable<List<PatientSearchResult>> {
    return database.patientSearchDao()
        .nameWithDobBounds(dateOfBirthUpperBound, dateOfBirthLowerBound, PatientStatus.ACTIVE)
        .toObservable()
        .switchMapSingle { searchPatientByName.search(name, it) }
        .zipWith(configProvider.toObservable())
        .map { (uuids, config) -> uuids.take(config.limitOfSearchResults) }
        .switchMapSingle { matchingUuidsSortedByScore ->
          when {
            matchingUuidsSortedByScore.isEmpty() -> Single.just(emptyList())
            else -> {
              database.patientSearchDao()
                  .searchByIds(matchingUuidsSortedByScore, PatientStatus.ACTIVE)
                  .map { results ->
                    val resultsByUuid = results.associateBy { it.uuid }
                    matchingUuidsSortedByScore.map { resultsByUuid[it]!! }
                  }
            }
          }
        }
        .compose(sortByCurrentFacility())
  }

  // TODO: Get user from caller.
  private fun sortByCurrentFacility(): ObservableTransformer<List<PatientSearchResult>, List<PatientSearchResult>> {
    return ObservableTransformer { searchResults ->
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
      database.patientDao()
          .updatePatientStatus(patientUuid, PatientStatus.DEAD)
      setSyncStatus(listOf(patientUuid), PENDING)
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
      database.addressDao().save(records.map { it.address })
      database.patientDao().save(records.map { it.patient })
      database.phoneNumberDao().save(records.filter { it.phoneNumbers.isNotEmpty() }.flatMap { it.phoneNumbers })
    }
  }

  private fun payloadToPatientProfile(patientPayload: PatientPayload): PatientProfile {
    return PatientProfile(
        patient = patientPayload.toDatabaseModel(newStatus = DONE),
        address = patientPayload.address.toDatabaseModel(),
        phoneNumbers = patientPayload.phoneNumbers?.map { it.toDatabaseModel(patientPayload.uuid) } ?: emptyList()
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
                createdAt = Instant.now(clock),
                updatedAt = Instant.now(clock),
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
                      updatedAt = Instant.now(clock),
                      computedDateOfBirth = LocalDate.now(clock).minusYears(personalDetails.age.toLong()))
                },

                status = PatientStatus.ACTIVE,

                createdAt = Instant.now(clock),
                updatedAt = Instant.now(clock),
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
                  createdAt = Instant.now(clock),
                  updatedAt = Instant.now(clock),
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
          updatedAt = Instant.now(clock),
          syncStatus = PENDING
      )
      database.patientDao().save(patientToSave)
    }
  }

  fun updateAddressForPatient(patientUuid: UUID, patientAddress: PatientAddress): Completable {
    return Completable
        .fromAction {
          val updatedPatientAddress = patientAddress.copy(updatedAt = Instant.now(clock))
          database.addressDao().save(updatedPatientAddress)
        }
        .andThen(Completable.fromAction {
          database.patientDao().updateSyncStatus(listOf(patientUuid), PENDING)
        })
  }

  fun updatePhoneNumberForPatient(patientUuid: UUID, phoneNumber: PatientPhoneNumber): Completable {
    return Single
        .fromCallable {
          phoneNumber.copy(updatedAt = Instant.now(clock))
        }
        .flatMapCompletable(this::savePhoneNumber)
        .andThen(Completable.fromAction {
          database.patientDao().updateSyncStatus(listOf(patientUuid), PENDING)
        })
  }

  fun createPhoneNumberForPatient(patientUuid: UUID, number: String, phoneNumberType: PatientPhoneNumberType, active: Boolean): Completable {
    return Single
        .fromCallable {
          val now = Instant.now(clock)

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

  fun phoneNumbers(patientUuid: UUID): Observable<Optional<PatientPhoneNumber>> {
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
    return Completable.fromCallable {
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
  }
}
