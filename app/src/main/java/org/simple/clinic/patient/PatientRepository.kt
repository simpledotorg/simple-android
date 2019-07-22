package org.simple.clinic.patient

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.patient.PatientSearchCriteria.ByName
import org.simple.clinic.patient.PatientSearchCriteria.ByPhoneNumber
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.BusinessId.MetaDataVersion
import org.simple.clinic.patient.businessid.BusinessIdMetaData.BpPassportMetaDataV1
import org.simple.clinic.patient.businessid.BusinessIdMetaDataAdapter
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.filter.SearchPatientByName
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toOptional
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
    private val numberValidator: PhoneNumberValidator,
    private val utcClock: UtcClock,
    private val searchPatientByName: SearchPatientByName,
    private val configProvider: Observable<PatientConfig>,
    private val reportsRepository: ReportsRepository,
    private val businessIdMetaDataAdapter: BusinessIdMetaDataAdapter,
    @Named("date_for_user_input") private val dateOfBirthFormat: DateTimeFormatter
) : SynceableRepository<PatientProfile, PatientPayload> {

  private var ongoingNewPatientEntry: OngoingNewPatientEntry = OngoingNewPatientEntry()

  fun search(criteria: PatientSearchCriteria): Observable<List<PatientSearchResult>> {
    return when (criteria) {
      is ByName -> searchByName(criteria.patientName)
      is ByPhoneNumber -> searchByPhoneNumber(criteria.phoneNumber)
    }
  }

  private fun searchByName(name: String): Observable<List<PatientSearchResult>> {
    return findPatientIdsMatchingName(name)
        .switchMapSingle { matchingUuidsSortedByScore ->
          when {
            matchingUuidsSortedByScore.isEmpty() -> Single.just(emptyList())
            else -> searchResultsByPatientUuids(matchingUuidsSortedByScore)
          }
        }
  }

  private fun searchResultsByPatientUuids(patientUuids: List<UUID>): Single<List<PatientSearchResult>> {
    return database.patientSearchDao()
        .searchByIds(patientUuids, PatientStatus.Active)
        .map { results ->
          // This is needed to maintain the order of the search results
          // so that its in the same order of the list of the UUIDs.
          // Otherwise, the order is dependent on the SQLite default
          // implementation.
          val resultsByUuid = results.associateBy { it.uuid }
          patientUuids.map { resultsByUuid.getValue(it) }
        }
  }

  private fun findPatientIdsMatchingName(name: String): Observable<List<UUID>> {
    val allPatientUuidsMatchingName = database
        .patientSearchDao()
        .nameAndId(PatientStatus.Active)
        .toObservable()
        .switchMapSingle { searchPatientByName.search(name, it) }

    return Observables.combineLatest(allPatientUuidsMatchingName, configProvider)
        .map { (uuids, config) -> uuids.take(config.limitOfSearchResults) }
  }

  private fun searchByPhoneNumber(phoneNumber: String): Observable<List<PatientSearchResult>> {
    return database
        .patientSearchDao()
        .searchByPhoneNumber(phoneNumber)
        .toObservable()
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
              newStatus = PatientStatus.Dead,
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
        .map { it.toDatabaseModel(patientPayload.uuid) }

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

  fun saveOngoingEntryAsPatient(
      loggedInUser: User,
      currentFacility: Facility
  ): Single<Patient> {
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

                status = PatientStatus.Active,

                createdAt = Instant.now(utcClock),
                updatedAt = Instant.now(utcClock),
                deletedAt = null,
                recordedAt = Instant.now(utcClock),
                syncStatus = PENDING)
          }
        }
        .cache()

    val patientSave = sharedPatient
        .flatMapCompletable { patient -> savePatient(patient) }

    val businessIdSave = cachedOngoingEntry
        .flatMapCompletable { entry ->
          if (entry.identifier == null) {
            Completable.complete()
          } else {
            addIdentifierToPatient(patientUuid, entry.identifier, loggedInUser, currentFacility).toCompletable()
          }
        }

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
        .andThen(businessIdSave)
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
        .andThen(setSyncStatus(listOf(patientUuid), PENDING))
  }

  fun updatePhoneNumberForPatient(patientUuid: UUID, phoneNumber: PatientPhoneNumber): Completable {
    return savePhoneNumber(phoneNumber.copy(updatedAt = Instant.now(utcClock)))
        .andThen(setSyncStatus(listOf(patientUuid), PENDING))
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
        .andThen(setSyncStatus(listOf(patientUuid), PENDING))
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

  fun compareAndUpdateRecordedAt(patientUuid: UUID, instantToCompare: Instant): Completable {
    return Completable.fromAction {
      database.patientDao().compareAndUpdateRecordedAt(
          patientUuid = patientUuid,
          instantToCompare = instantToCompare,
          pendingStatus = PENDING,
          updatedAt = Instant.now(utcClock)
      )
    }
  }

  fun updateRecordedAt(patientUuid: UUID): Completable {
    return Completable.fromAction {
      database.patientDao().updateRecordedAt(
          patientUuid = patientUuid,
          pendingStatus = PENDING,
          updatedAt = Instant.now(utcClock))
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
            database.medicalHistoryDao().clear()
          }
        }
        .andThen(reportsRepository.deleteReportsFile().toCompletable())
  }

  fun recentPatients(facilityUuid: UUID, limit: Int): Observable<List<RecentPatient>> =
      database.recentPatientDao()
          .recentPatients(facilityUuid, Scheduled, Manual, PatientStatus.Active, limit)
          .toObservable()

  fun recentPatients(facilityUuid: UUID): Observable<List<RecentPatient>> =
      database.recentPatientDao()
          .recentPatients(facilityUuid, Scheduled, Manual, PatientStatus.Active)
          .toObservable()

  override fun pendingSyncRecordCount(): Observable<Int> {
    return database.patientDao()
        .patientCount(PENDING)
        .toObservable()
  }

  fun addIdentifierToPatient(
      patientUuid: UUID,
      identifier: Identifier,
      assigningUser: User,
      assigningFacility: Facility
  ): Single<BusinessId> {
    val businessIdStream = createBusinessIdMetaDataForIdentifier(identifier.type, assigningUser, assigningFacility)
        .map { metaAndVersion ->
          val now = Instant.now(utcClock)
          BusinessId(
              uuid = UUID.randomUUID(),
              patientUuid = patientUuid,
              identifier = identifier,
              metaDataVersion = metaAndVersion.metaDataVersion,
              metaData = metaAndVersion.metaData,
              createdAt = now,
              updatedAt = now,
              deletedAt = null
          )
        }

    return businessIdStream
        .flatMap { businessId -> saveBusinessId(businessId).toSingleDefault(businessId) }
        .flatMap { businessId -> setSyncStatus(listOf(patientUuid), PENDING).toSingleDefault(businessId) }
  }

  private fun saveBusinessId(businessId: BusinessId): Completable {
    return Completable.fromAction {
      database.businessIdDao().save(listOf(businessId))
    }
  }

  private fun createBusinessIdMetaDataForIdentifier(
      identifierType: Identifier.IdentifierType,
      assigningUser: User,
      assigningFacility: Facility
  ): Single<BusinessIdMetaAndVersion> {
    return when (identifierType) {
      BpPassport -> createBpPassportMetaData(assigningUser, assigningFacility)
      else -> Single.error<BusinessIdMetaAndVersion>(IllegalArgumentException("Cannot create meta for identifier of type: $identifierType"))
    }
  }

  private fun createBpPassportMetaData(assigningUser: User, assigningFacility: Facility): Single<BusinessIdMetaAndVersion> {
    return Single.just(BpPassportMetaDataV1(assigningUserUuid = assigningUser.uuid, assigningFacilityUuid = assigningFacility.uuid))
        .map { businessIdMetaDataAdapter.serialize(it, MetaDataVersion.BpPassportMetaDataV1) to MetaDataVersion.BpPassportMetaDataV1 }
        .map { (meta, version) -> BusinessIdMetaAndVersion(meta, version) }
  }

  fun findPatientWithBusinessId(identifier: String): Observable<Optional<Patient>> {
    return database
        .patientDao()
        .findPatientsWithBusinessId(identifier)
        .map { patients ->
          if (patients.isEmpty()) {
            None
          } else {
            patients.first().toOptional()
          }
        }
        .toObservable()

  }

  fun bpPassportForPatient(patientUuid: UUID): Observable<Optional<BusinessId>> {
    return database
        .businessIdDao()
        .latestForPatientByType(patientUuid, BpPassport)
        .map { bpPassports ->
          if (bpPassports.isEmpty()) {
            None
          } else {
            bpPassports.first().toOptional()
          }
        }
        .toObservable()
  }

  fun isPatientDefaulter(patientUuid: UUID): Observable<Boolean> {
    return database
        .patientDao()
        .isPatientDefaulter(
            patientUuid = patientUuid
        ).toObservable()
  }

  fun hasPatientChangedSince(patientUuid: UUID, instant: Instant): Observable<Boolean> {
    return database
        .patientDao()
        .hasPatientChangedSince(
            patientUuid = patientUuid,
            instantToCompare = instant,
            pendingStatus = PENDING
        )
        .toObservable()
  }

  fun allPatientsInFacility(facility: Facility): Observable<List<PatientSearchResult>> {
    return database
        .patientSearchDao()
        .searchInFacilityAndSortByName(facility.uuid, PatientStatus.Active)
        .toObservable()
  }

  private data class BusinessIdMetaAndVersion(val metaData: String, val metaDataVersion: MetaDataVersion)
}
