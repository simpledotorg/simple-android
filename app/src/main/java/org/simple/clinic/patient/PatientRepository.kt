package org.simple.clinic.patient

import androidx.annotation.VisibleForTesting
import androidx.paging.PagingSource
import com.squareup.moshi.JsonAdapter
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.contactpatient.ContactPatientProfile
import org.simple.clinic.di.AppScope
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.NumericCriteria
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.BusinessId.MetaDataVersion
import org.simple.clinic.patient.businessid.BusinessIdMetaData
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toOptional
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID
import javax.inject.Inject

typealias PatientUuid = UUID

@AppScope
class PatientRepository @Inject constructor(
    private val database: AppDatabase,
    private val utcClock: UtcClock,
    private val reportsRepository: ReportsRepository,
    private val businessIdMetaDataMoshiAdapter: JsonAdapter<BusinessIdMetaData>
) : SynceableRepository<PatientProfile, PatientPayload> {

  private var ongoingNewPatientEntry: OngoingNewPatientEntry = OngoingNewPatientEntry()

  fun search(criteria: PatientSearchCriteria, facilityId: UUID): PagingSource<Int, PatientSearchResult> {
    val query = when (criteria) {
      is Name -> criteria.patientName
      is NumericCriteria -> criteria.numericCriteria
    }
    return database.patientSearchDao().search(query, facilityId)
  }

  fun patient(uuid: UUID): Observable<Optional<Patient>> {
    return database.patientDao()
        .patient(uuid)
        .toObservable()
        .map { patients -> Optional.ofNullable(patients.firstOrNull()) }
  }

  fun patientImmediate(uuid: UUID): Patient? {
    return database.patientDao()
        .patientImmediate(uuid)
  }

  fun updatePatientStatusToDead(patientUuid: UUID) {
    database
        .patientDao()
        .updatePatientStatus(
            uuid = patientUuid,
            newStatus = PatientStatus.Dead,
            newSyncStatus = PENDING,
            newUpdatedAt = Instant.now(utcClock)
        )
  }

  fun updatePatientStatusToMigrated(patientUuid: UUID) {
    database
        .patientDao()
        .updatePatientStatus(
            uuid = patientUuid,
            newStatus = PatientStatus.Migrated,
            newSyncStatus = PENDING,
            newUpdatedAt = Instant.now(utcClock)
        )
  }

  fun recordsWithSyncStatus(syncStatus: SyncStatus): List<PatientProfile> {
    return database.patientDao().recordsWithSyncStatus(syncStatus)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    database.patientDao().updateSyncStatus(from, to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    database.patientDao().updateSyncStatusForIds(ids, to)
  }

  override fun recordCount(): Observable<Int> {
    return database.patientDao()
        .patientCount()
        .toObservable()
  }

  override fun mergeWithLocalData(payloads: List<PatientPayload>) {
    val dirtyRecords = database.patientDao().recordIdsWithSyncStatus(PENDING)

    val payloadsToSave = payloads
        .filterNot { it.uuid in dirtyRecords }
        .map(::payloadToPatientProfile)

    saveRecords(payloadsToSave)
  }

  override fun save(records: List<PatientProfile>) {
    saveRecords(records)
  }

  private fun saveRecords(records: List<PatientProfile>) {
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

  fun ongoingEntry(): OngoingNewPatientEntry {
    return ongoingNewPatientEntry
  }

  fun saveOngoingEntry(ongoingEntry: OngoingNewPatientEntry) {
    ongoingNewPatientEntry = ongoingEntry
  }

  fun saveOngoingEntryAsPatient(
      patientEntry: OngoingNewPatientEntry,
      loggedInUser: User,
      facility: Facility,
      patientUuid: UUID,
      addressUuid: UUID,
      supplyUuidForBpPassport: () -> UUID,
      supplyUuidForAlternativeId: () -> UUID,
      supplyUuidForPhoneNumber: () -> UUID,
      dateOfBirthFormatter: DateTimeFormatter
  ): PatientProfile {
    val patientProfile = convertOngoingPatientEntryToPatientProfile(
        patientEntry = patientEntry,
        loggedInUser = loggedInUser,
        facility = facility,
        patientUuid = patientUuid,
        addressUuid = addressUuid,
        supplyUuidForBpPassport = supplyUuidForBpPassport,
        supplyUuidForAlternativeId = supplyUuidForAlternativeId,
        supplyUuidForPhoneNumber = supplyUuidForPhoneNumber,
        dateOfBirthFormatter = dateOfBirthFormatter
    )

    saveRecords(listOf(patientProfile))

    return patientProfile
  }

  private fun convertOngoingPatientEntryToPatientProfile(
      patientEntry: OngoingNewPatientEntry,
      loggedInUser: User,
      facility: Facility,
      patientUuid: UUID,
      addressUuid: UUID,
      supplyUuidForBpPassport: () -> UUID,
      supplyUuidForAlternativeId: () -> UUID,
      supplyUuidForPhoneNumber: () -> UUID,
      dateOfBirthFormatter: DateTimeFormatter
  ): PatientProfile {
    return with(patientEntry) {
      requireNotNull(personalDetails)
      requireNotNull(address)

      val age = personalDetails.age?.toInt()
      val dateOfBirth = personalDetails.dateOfBirth?.let {
        dateOfBirthFormatter.parse(it, LocalDate::from)
      }

      val patient = Patient(
          uuid = patientUuid,
          addressUuid = addressUuid,
          fullName = personalDetails.fullName,
          gender = personalDetails.gender!!,
          ageDetails = PatientAgeDetails(
              ageValue = age,
              ageUpdatedAt = if (age != null) Instant.now(utcClock) else null,
              dateOfBirth = dateOfBirth
          ),
          status = PatientStatus.Active,
          createdAt = Instant.now(utcClock),
          updatedAt = Instant.now(utcClock),
          deletedAt = null,
          recordedAt = Instant.now(utcClock),
          syncStatus = PENDING,
          reminderConsent = reminderConsent,
          deletedReason = null,
          registeredFacilityId = facility.uuid,
          assignedFacilityId = facility.uuid,
          retainUntil = null
      )

      val address = PatientAddress(
          uuid = addressUuid,
          streetAddress = address.streetAddress,
          colonyOrVillage = address.colonyOrVillage,
          zone = address.zone,
          district = address.district,
          state = address.state,
          country = facility.country,
          createdAt = Instant.now(utcClock),
          updatedAt = Instant.now(utcClock),
          deletedAt = null
      )

      val phoneNumbers = createPhoneNumbersFromOngoingPatientEntry(
          ongoingEntry = this,
          patientUuid = patientUuid,
          supplyUuidForPhoneNumber = supplyUuidForPhoneNumber
      )

      val businessIds = createBusinessIdsFromOngoingPatientEntry(
          ongoingEntry = this,
          patientUuid = patientUuid,
          user = loggedInUser,
          supplyUuidForBpPassport = supplyUuidForBpPassport,
          supplyUuidForAlternativeId = supplyUuidForAlternativeId
      )

      PatientProfile(
          patient = patient,
          address = address,
          phoneNumbers = phoneNumbers,
          businessIds = businessIds
      )
    }
  }

  private fun createPhoneNumbersFromOngoingPatientEntry(
      ongoingEntry: OngoingNewPatientEntry,
      patientUuid: UUID,
      supplyUuidForPhoneNumber: () -> UUID,
  ): List<PatientPhoneNumber> {
    return with(ongoingEntry) {
      val phoneNumbers = mutableListOf<PatientPhoneNumber>()

      if (phoneNumber != null) {
        val patientPhoneNumber = PatientPhoneNumber(
            uuid = supplyUuidForPhoneNumber(),
            patientUuid = patientUuid,
            number = phoneNumber.number,
            phoneType = phoneNumber.type,
            active = phoneNumber.active,
            createdAt = Instant.now(utcClock),
            updatedAt = Instant.now(utcClock),
            deletedAt = null
        )

        phoneNumbers.add(patientPhoneNumber)
      }

      phoneNumbers.toList()
    }
  }

  private fun createBusinessIdsFromOngoingPatientEntry(
      ongoingEntry: OngoingNewPatientEntry,
      patientUuid: UUID,
      user: User,
      supplyUuidForBpPassport: () -> UUID,
      supplyUuidForAlternativeId: () -> UUID
  ): List<BusinessId> {

    return with(ongoingEntry) {
      val businessIds = mutableListOf<BusinessId>()

      if (identifier != null && identifier.value.isNotBlank()) {
        val bpPassport = createBusinessIdFromIdentifier(
            id = supplyUuidForBpPassport(),
            patientUuid = patientUuid,
            identifier = identifier,
            user = user
        )
        businessIds.add(bpPassport)
      }

      if (alternateId != null && alternateId.value.isNotBlank()) {
        val alternativeBusinessId = createBusinessIdFromIdentifier(
            id = supplyUuidForAlternativeId(),
            patientUuid = patientUuid,
            identifier = alternateId,
            user = user
        )

        businessIds.add(alternativeBusinessId)
      }

      businessIds.toList()
    }
  }

  fun createBusinessIdFromIdentifier(
      id: UUID,
      patientUuid: UUID,
      identifier: Identifier,
      user: User
  ): BusinessId {
    val metaAndVersion = createBusinessIdMetaDataForIdentifier(identifier.type, user)
    val now = Instant.now(utcClock)

    val identifierSearchHelp = when (identifier.type) {
      BpPassport -> BpPassport.shortCode(identifier)
      else -> identifier.value
    }

    return BusinessId(
        uuid = id,
        patientUuid = patientUuid,
        identifier = identifier,
        metaDataVersion = metaAndVersion.metaDataVersion,
        metaData = metaAndVersion.metaData,
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
        searchHelp = identifierSearchHelp
    )
  }

  fun updatePatient(patient: Patient): Completable {
    return Completable.fromAction {
      val patientToSave = patient.copy(
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
          setSyncStatus(listOf(patientUuid), PENDING)
        }
  }

  fun updatePhoneNumberForPatient(patientUuid: UUID, phoneNumber: PatientPhoneNumber): Completable {
    return savePhoneNumber(phoneNumber.copy(updatedAt = Instant.now(utcClock)))
        .andThen(Completable.fromAction { setSyncStatus(listOf(patientUuid), PENDING) })
  }

  fun createPhoneNumberForPatient(
      uuid: UUID,
      patientUuid: UUID,
      numberDetails: PhoneNumberDetails,
      active: Boolean
  ): Completable {
    return Single
        .fromCallable {
          val now = Instant.now(utcClock)

          PatientPhoneNumber(
              uuid = uuid,
              patientUuid = patientUuid,
              number = numberDetails.number,
              phoneType = numberDetails.type,
              active = active,
              createdAt = now,
              updatedAt = now,
              deletedAt = null
          )
        }
        .flatMapCompletable(this::savePhoneNumber)
        .andThen(Completable.fromAction { setSyncStatus(listOf(patientUuid), PENDING) })
  }

  fun address(addressUuid: UUID): Observable<Optional<PatientAddress>> {
    return database.addressDao()
        .address(addressUuid)
        .toObservable()
        .map { addresses -> Optional.ofNullable(addresses.firstOrNull()) }
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
        .map { numbers -> Optional.ofNullable(numbers.firstOrNull()) }
  }

  fun latestPhoneNumberForPatient(patientUuid: UUID): Optional<PatientPhoneNumber> {
    return database.phoneNumberDao().latestPhoneNumber(patientUuid).toOptional()
  }

  fun compareAndUpdateRecordedAt(patientUuid: UUID, instantToCompare: Instant) {
    return database
        .patientDao()
        .compareAndUpdateRecordedAt(
            patientUuid = patientUuid,
            instantToCompare = instantToCompare,
            pendingStatus = PENDING,
            updatedAt = Instant.now(utcClock)
        )
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
        .fromCallable { database.clearAppData() }
        .doOnComplete { reportsRepository.deleteReports() }
  }

  fun recentPatients(facilityUuid: UUID, limit: Int): Observable<List<RecentPatient>> =
      database.recentPatientDao()
          .recentPatientsWithLimit(facilityUuid, Scheduled, Manual, PatientStatus.Active, limit)
          .toObservable()

  fun recentPatients(facilityUuid: UUID): PagingSource<Int, RecentPatient> =
      database.recentPatientDao()
          .recentPatients(
              facilityUuid = facilityUuid,
              appointmentStatus = Scheduled,
              appointmentType = Manual,
              patientStatus = PatientStatus.Active
          )

  fun allColoniesOrVillagesInPatientAddress(): List<String> =
      database.addressDao()
          .getColonyOrVillages()

  override fun pendingSyncRecordCount(): Observable<Int> {
    return database.patientDao()
        .patientCountWithStatus(PENDING)
        .toObservable()
  }

  override fun pendingSyncRecords(limit: Int, offset: Int): List<PatientProfile> {
    return database
        .patientDao()
        .profilesWithSyncStatusBatched(
            syncStatus = PENDING,
            limit = limit,
            offset = offset
        )
  }

  fun addIdentifierToPatient(
      uuid: UUID,
      patientUuid: UUID,
      identifier: Identifier,
      assigningUser: User
  ): Single<BusinessId> {
    val businessId = createBusinessIdFromIdentifier(
        id = uuid,
        patientUuid = patientUuid,
        identifier = identifier,
        user = assigningUser
    )

    return saveBusinessId(businessId)
        .toSingleDefault(businessId)
        .doOnSuccess { setSyncStatus(listOf(patientUuid), PENDING) }
  }

  fun saveBusinessId(businessId: BusinessId): Completable {
    return Completable.fromAction {
      database.businessIdDao().save(listOf(businessId))
    }
  }

  private fun saveBusinessIds(businessIds: List<BusinessId>) {
    database.businessIdDao().save(businessIds)
  }

  private fun createBusinessIdMetaDataForIdentifier(
      identifierType: IdentifierType,
      assigningUser: User
  ): BusinessIdMetaAndVersion {
    val metaData = BusinessIdMetaData(
        assigningUserUuid = assigningUser.uuid,
        assigningFacilityUuid = assigningUser.currentFacilityUuid
    )
    val metaDataVersion = MetaDataVersion
        .forIdentifierType(identifierType)
        .orElseThrow { IllegalArgumentException("Cannot create meta for identifier of type: $identifierType") }

    val serialized = businessIdMetaDataMoshiAdapter.toJson(metaData)

    return BusinessIdMetaAndVersion(
        metaData = serialized,
        metaDataVersion = metaDataVersion
    )
  }

  fun findPatientWithBusinessId(identifier: String): Observable<Optional<Patient>> {
    return database
        .patientDao()
        .findPatientsWithBusinessId(identifier)
        .map { patients -> Optional.ofNullable(patients.firstOrNull()) }
        .toObservable()
  }

  fun findPatientsWithBusinessId(identifier: String): List<Patient> {
    return database
        .patientDao()
        .findPatientsWithBusinessIdImmediate(identifier)
  }

  fun bpPassportForPatient(patientUuid: UUID): Optional<BusinessId> {
    val bpPassportsForPatient = database
        .businessIdDao()
        .latestForPatientByTypeImmediate(patientUuid, BpPassport)

    return bpPassportsForPatient.firstOrNull().toOptional()
  }

  fun bangladeshNationalIdForPatient(patientUuid: UUID): Observable<Optional<BusinessId>> {
    return database
        .businessIdDao()
        .latestForPatientByType(patientUuid, BangladeshNationalId)
        .map { bangladeshNationalId -> Optional.ofNullable(bangladeshNationalId.firstOrNull()) }
        .toObservable()
  }

  fun deleteBusinessId(businessId: BusinessId): Completable {
    return Completable.fromAction {
      val now = Instant.now(utcClock)
      val deletedBusinessId = businessId.copy(
          updatedAt = now,
          deletedAt = now
      )
      database.businessIdDao().save(listOf(deletedBusinessId))
    }
  }

  fun isPatientDefaulter(patientUuid: UUID): Boolean {
    return database
        .patientDao()
        .isPatientDefaulter(patientUuid)
  }

  fun allPatientsInFacility(facilityId: UUID): PagingSource<Int, PatientSearchResult> {
    return database
        .patientSearchDao()
        .allPatientsInFacility(facilityId = facilityId, status = PatientStatus.Active)
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  fun haveBpsForPatientChangedSince(patientUuid: UUID, instant: Instant): Boolean {
    return database
        .bloodPressureDao()
        .haveBpsForPatientChangedSince(
            patientUuid = patientUuid,
            instantToCompare = instant,
            pendingStatus = PENDING
        )
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  fun haveBloodSugarsForPatientChangedSince(patientUuid: UUID, instant: Instant): Boolean {
    return database
        .bloodSugarDao()
        .haveBloodSugarsForPatientChangedSince(
            patientUuid = patientUuid,
            instantToCompare = instant,
            pendingStatus = PENDING
        )
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  fun hasPrescriptionForPatientChangedSince(patientUuid: UUID, instant: Instant): Boolean {
    return database
        .prescriptionDao()
        .hasPrescriptionForPatientChangedSince(
            patientUuid = patientUuid,
            instantToCompare = instant,
            pendingStatus = PENDING
        )
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  fun hasMedicalHistoryForPatientChangedSince(patientUuid: UUID, instant: Instant): Boolean {
    return database
        .medicalHistoryDao()
        .hasMedicalHistoryForPatientChangedSince(
            patientUuid = patientUuid,
            instantToCompare = instant,
            pendingStatus = PENDING
        )
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  fun hasPatientChangedSince(patientUuid: UUID, instant: Instant): Boolean {
    return database
        .patientDao()
        .hasPatientChangedSince(
            patientUuid = patientUuid,
            instantToCompare = instant,
            pendingStatus = PENDING
        )
  }

  fun hasPatientDataChangedSince(patientUuid: UUID, timestamp: Instant): Boolean {
    val patientChangedSince = hasPatientChangedSince(patientUuid, timestamp)
    val bpsChangedSince = haveBpsForPatientChangedSince(patientUuid, timestamp)
    val prescriptionsChangedSince = hasPrescriptionForPatientChangedSince(patientUuid, timestamp)
    val medicalHistoryChangedSince = hasMedicalHistoryForPatientChangedSince(patientUuid, timestamp)
    val bloodSugarsChangedSince = haveBloodSugarsForPatientChangedSince(patientUuid, timestamp)

    return patientChangedSince
        .or(bpsChangedSince)
        .or(prescriptionsChangedSince)
        .or(medicalHistoryChangedSince)
        .or(bloodSugarsChangedSince)
  }

  fun hasPatientMeasurementDataChangedSince(patientUuid: UUID, timestamp: Instant): Boolean {
    val bpsChangedSince = haveBpsForPatientChangedSince(patientUuid, timestamp)
    val bloodSugarsChangedSince = haveBloodSugarsForPatientChangedSince(patientUuid, timestamp)
    val prescriptionsChangedSince = hasPrescriptionForPatientChangedSince(patientUuid, timestamp)

    return bpsChangedSince
        .or(bloodSugarsChangedSince)
        .or(prescriptionsChangedSince)
  }

  fun patientProfileImmediate(patientUuid: UUID): Optional<PatientProfile> {
    return database.patientDao().patientProfileImmediate(patientUuid).toOptional()
  }

  fun patientProfile(patientUuid: UUID): Observable<Optional<PatientProfile>> {
    return database
        .patientDao()
        .patientProfile(patientUuid)
        .map { patientProfiles -> Optional.ofNullable(patientProfiles.firstOrNull()) }
  }

  fun deletePatient(patientUuid: UUID, deletedReason: DeletedReason) {
    val now = Instant.now(utcClock)
    return database.patientDao().deletePatient(
        patientUuid = patientUuid,
        updatedAt = now,
        deletedAt = now,
        deletedReason = deletedReason,
        pendingStatus = PENDING
    )
  }

  fun updateAssignedFacilityId(patientId: UUID, assignedFacilityId: UUID) {
    val now = Instant.now(utcClock)
    database.patientDao().updateAssignedFacilityId(
        patientUuid = patientId,
        assignedFacilityId = assignedFacilityId,
        updatedAt = now,
        pendingStatus = PENDING
    )
  }

  fun allPatientProfiles(): List<PatientProfile> {
    return database.patientDao().allPatientProfiles()
  }

  fun saveCompleteMedicalRecord(medicalRecords: List<CompleteMedicalRecord>) {
    medicalRecords.forEach { medicalRecord ->
      database.runInTransaction {
        saveRecords(listOf(medicalRecord.patient))
        if (medicalRecord.medicalHistory != null) {
          database.medicalHistoryDao().save(medicalRecord.medicalHistory)
        }
        database.bloodPressureDao().save(medicalRecord.bloodPressures)
        database.bloodSugarDao().save(medicalRecord.bloodSugars)
        database.appointmentDao().save(medicalRecord.appointments)
        database.prescriptionDao().save(medicalRecord.prescribedDrugs)
      }
    }
  }

  fun addIdentifiersToPatient(
      patientUuid: UUID,
      businessIds: List<BusinessId>
  ) {
    database.runInTransaction {
      saveBusinessIds(businessIds)
      setSyncStatus(listOf(patientUuid), PENDING)
    }
  }

  fun contactPatientProfileImmediate(patientUuid: UUID): ContactPatientProfile {
    return database.patientDao().contactPatientProfileImmediate(patientUuid)
  }

  private data class BusinessIdMetaAndVersion(
      val metaData: String,
      val metaDataVersion: MetaDataVersion
  )
}
