package org.simple.clinic.storage

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.sharedTestCode.TestData
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.SyncStatus
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class PurgeDatabaseAndroidTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  private val patientDao by lazy { appDatabase.patientDao() }

  private val patientAddressDao by lazy { appDatabase.addressDao() }

  private val phoneNumberDao by lazy { appDatabase.phoneNumberDao() }

  private val medicalHistoryDao by lazy { appDatabase.medicalHistoryDao() }

  private val businessIdDao by lazy { appDatabase.businessIdDao() }

  private val bloodPressureDao by lazy { appDatabase.bloodPressureDao() }

  private val bloodSugarDao by lazy { appDatabase.bloodSugarDao() }

  private val appointmentDao by lazy { appDatabase.appointmentDao() }

  private val prescribedDrugsDao by lazy { appDatabase.prescriptionDao() }

  private val callResultDao by lazy { appDatabase.callResultDao() }

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_patients() {
    // given
    val deletedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("2463850b-c294-4db4-906b-68d9ea8e99a1"),
        syncStatus = SyncStatus.DONE,
        generatePhoneNumber = true,
        generateBusinessId = true,
        patientDeletedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val notDeletedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("427bfcf1-1b6c-42fa-903b-8527c594a0f9"),
        syncStatus = SyncStatus.DONE,
        generatePhoneNumber = true,
        generateBusinessId = true,
        patientDeletedAt = null
    )
    val deletedButUnsyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("3f31b541-afae-41a3-9eb3-6e269ad6fb5d"),
        syncStatus = SyncStatus.PENDING,
        generatePhoneNumber = true,
        generateBusinessId = true,
        patientDeletedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    patientAddressDao.save(listOf(deletedPatientProfile.address, notDeletedPatientProfile.address, deletedButUnsyncedPatientProfile.address))
    patientDao.save(listOf(deletedPatientProfile.patient, notDeletedPatientProfile.patient, deletedButUnsyncedPatientProfile.patient))
    phoneNumberDao.save(deletedPatientProfile.phoneNumbers + notDeletedPatientProfile.phoneNumbers + deletedButUnsyncedPatientProfile.phoneNumbers)
    businessIdDao.save(deletedPatientProfile.businessIds + notDeletedPatientProfile.businessIds + deletedButUnsyncedPatientProfile.businessIds)

    assertThat(patientDao.patientProfileImmediate(deletedPatientProfile.patientUuid)).isEqualTo(deletedPatientProfile)
    assertThat(patientDao.patientProfileImmediate(notDeletedPatientProfile.patientUuid)).isEqualTo(notDeletedPatientProfile)
    assertThat(patientDao.patientProfileImmediate(deletedButUnsyncedPatientProfile.patientUuid)).isEqualTo(deletedButUnsyncedPatientProfile)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(patientDao.patientProfileImmediate(deletedPatientProfile.patientUuid)).isNull()
    assertThat(patientDao.patientProfileImmediate(notDeletedPatientProfile.patientUuid)).isEqualTo(notDeletedPatientProfile)
    assertThat(patientDao.patientProfileImmediate(deletedButUnsyncedPatientProfile.patientUuid)).isEqualTo(deletedButUnsyncedPatientProfile)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_phonenumbers() {
    // given
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("57d2ef99-59e7-4dc5-9cc5-4fe6917386b7"),
        syncStatus = SyncStatus.DONE,
        generatePhoneNumber = false
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("00001c29-a108-49b7-8db2-e867782c633f"),
        syncStatus = SyncStatus.PENDING,
        generatePhoneNumber = false
    )
    val deletedPhoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("805b93ac-c53f-4a66-b845-e3c458fa0aa6"),
        patientUuid = syncedPatientProfile.patientUuid,
        deletedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val notDeletedPhoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("3b0b66de-8760-4415-9edc-fc8d684c4e12"),
        patientUuid = syncedPatientProfile.patientUuid,
        deletedAt = null
    )
    val deletedButUnsyncedPhoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("5e7a68fe-c5b6-4e2f-b668-9226c7123421"),
        patientUuid = notSyncedPatientProfile.patientUuid,
        deletedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    patientAddressDao.save(listOf(syncedPatientProfile.address, notSyncedPatientProfile.address))
    patientDao.save(listOf(syncedPatientProfile.patient, notSyncedPatientProfile.patient))
    phoneNumberDao.save(listOf(deletedPhoneNumber, notDeletedPhoneNumber, deletedButUnsyncedPhoneNumber))

    assertThat(phoneNumberDao.phoneNumber(syncedPatientProfile.patientUuid).blockingFirst()).containsExactly(deletedPhoneNumber, notDeletedPhoneNumber)
    assertThat(phoneNumberDao.phoneNumber(notSyncedPatientProfile.patientUuid).blockingFirst()).containsExactly(deletedButUnsyncedPhoneNumber)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(phoneNumberDao.phoneNumber(syncedPatientProfile.patientUuid).blockingFirst()).containsExactly(notDeletedPhoneNumber)
    assertThat(phoneNumberDao.phoneNumber(notSyncedPatientProfile.patientUuid).blockingFirst()).containsExactly(deletedButUnsyncedPhoneNumber)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_business_ids() {
    // given
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("57d2ef99-59e7-4dc5-9cc5-4fe6917386b7"),
        syncStatus = SyncStatus.DONE,
        generateBusinessId = false
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("00001c29-a108-49b7-8db2-e867782c633f"),
        syncStatus = SyncStatus.PENDING,
        generateBusinessId = false
    )
    val deletedBusinessId = TestData.businessId(
        uuid = UUID.fromString("805b93ac-c53f-4a66-b845-e3c458fa0aa6"),
        patientUuid = syncedPatientProfile.patientUuid,
        deletedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val notDeletedBusinessId = TestData.businessId(
        uuid = UUID.fromString("3b0b66de-8760-4415-9edc-fc8d684c4e12"),
        patientUuid = syncedPatientProfile.patientUuid,
        deletedAt = null
    )
    val deletedButUnsyncedBusinessId = TestData.businessId(
        uuid = UUID.fromString("5e7a68fe-c5b6-4e2f-b668-9226c7123421"),
        patientUuid = notSyncedPatientProfile.patientUuid,
        deletedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    patientAddressDao.save(listOf(syncedPatientProfile.address, notSyncedPatientProfile.address))
    patientDao.save(listOf(syncedPatientProfile.patient, notSyncedPatientProfile.patient))
    businessIdDao.save(listOf(deletedBusinessId, notDeletedBusinessId, deletedButUnsyncedBusinessId))

    assertThat(businessIdDao.get(deletedBusinessId.uuid)).isEqualTo(deletedBusinessId)
    assertThat(businessIdDao.get(notDeletedBusinessId.uuid)).isEqualTo(notDeletedBusinessId)
    assertThat(businessIdDao.get(deletedButUnsyncedBusinessId.uuid)).isEqualTo(deletedButUnsyncedBusinessId)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(businessIdDao.get(deletedBusinessId.uuid)).isNull()
    assertThat(businessIdDao.get(notDeletedBusinessId.uuid)).isEqualTo(notDeletedBusinessId)
    assertThat(businessIdDao.get(deletedButUnsyncedBusinessId.uuid)).isEqualTo(deletedButUnsyncedBusinessId)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_blood_pressure_measurements() {
    // given
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("57d2ef99-59e7-4dc5-9cc5-4fe6917386b7"),
        syncStatus = SyncStatus.DONE,
        generateBusinessId = false
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("00001c29-a108-49b7-8db2-e867782c633f"),
        syncStatus = SyncStatus.PENDING,
        generateBusinessId = false
    )
    val deletedBloodPressureMeasurement = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val notDeletedBloodPressureMeasurement = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val deletedButUnsyncedBloodPressureMeasurement = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.PENDING,
        patientUuid = notSyncedPatientProfile.patientUuid
    )

    bloodPressureDao.save(listOf(deletedBloodPressureMeasurement, deletedButUnsyncedBloodPressureMeasurement, notDeletedBloodPressureMeasurement))

    assertThat(bloodPressureDao.getOne(deletedBloodPressureMeasurement.uuid)).isEqualTo(deletedBloodPressureMeasurement)
    assertThat(bloodPressureDao.getOne(notDeletedBloodPressureMeasurement.uuid)).isEqualTo(notDeletedBloodPressureMeasurement)
    assertThat(bloodPressureDao.getOne(deletedButUnsyncedBloodPressureMeasurement.uuid)).isEqualTo(deletedButUnsyncedBloodPressureMeasurement)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(bloodPressureDao.getOne(deletedBloodPressureMeasurement.uuid)).isNull()
    assertThat(bloodPressureDao.getOne(notDeletedBloodPressureMeasurement.uuid)).isEqualTo(notDeletedBloodPressureMeasurement)
    assertThat(bloodPressureDao.getOne(deletedButUnsyncedBloodPressureMeasurement.uuid)).isEqualTo(deletedButUnsyncedBloodPressureMeasurement)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_blood_sugar_measurements() {
    // given
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("57d2ef99-59e7-4dc5-9cc5-4fe6917386b7"),
        syncStatus = SyncStatus.DONE,
        generateBusinessId = false
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("00001c29-a108-49b7-8db2-e867782c633f"),
        syncStatus = SyncStatus.PENDING,
        generateBusinessId = false
    )
    val deletedBloodSugarMeasurement = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val notDeletedBloodSugarMeasurement = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val deletedButUnsyncedBloodSugarMeasurement = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.PENDING,
        patientUuid = notSyncedPatientProfile.patientUuid
    )

    bloodSugarDao.save(listOf(deletedBloodSugarMeasurement, deletedButUnsyncedBloodSugarMeasurement, notDeletedBloodSugarMeasurement))

    assertThat(bloodSugarDao.getOne(deletedBloodSugarMeasurement.uuid)).isEqualTo(deletedBloodSugarMeasurement)
    assertThat(bloodSugarDao.getOne(notDeletedBloodSugarMeasurement.uuid)).isEqualTo(notDeletedBloodSugarMeasurement)
    assertThat(bloodSugarDao.getOne(deletedButUnsyncedBloodSugarMeasurement.uuid)).isEqualTo(deletedButUnsyncedBloodSugarMeasurement)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(bloodSugarDao.getOne(deletedBloodSugarMeasurement.uuid)).isNull()
    assertThat(bloodSugarDao.getOne(notDeletedBloodSugarMeasurement.uuid)).isEqualTo(notDeletedBloodSugarMeasurement)
    assertThat(bloodSugarDao.getOne(deletedButUnsyncedBloodSugarMeasurement.uuid)).isEqualTo(deletedButUnsyncedBloodSugarMeasurement)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_prescriptions() {
    // given
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("57d2ef99-59e7-4dc5-9cc5-4fe6917386b7"),
        syncStatus = SyncStatus.DONE,
        generateBusinessId = false
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("00001c29-a108-49b7-8db2-e867782c633f"),
        syncStatus = SyncStatus.PENDING,
        generateBusinessId = false
    )
    val deletedPrescription = TestData.prescription(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        isDeleted = true,
        syncStatus = SyncStatus.DONE,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val notDeletedPrescription = TestData.prescription(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        isDeleted = false,
        syncStatus = SyncStatus.DONE,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val deletedButUnsyncedPrescripion = TestData.prescription(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        isDeleted = true,
        syncStatus = SyncStatus.PENDING,
        patientUuid = notSyncedPatientProfile.patientUuid
    )

    prescribedDrugsDao.save(listOf(deletedPrescription, deletedButUnsyncedPrescripion, notDeletedPrescription))

    assertThat(prescribedDrugsDao.getOne(deletedPrescription.uuid)).isEqualTo(deletedPrescription)
    assertThat(prescribedDrugsDao.getOne(notDeletedPrescription.uuid)).isEqualTo(notDeletedPrescription)
    assertThat(prescribedDrugsDao.getOne(deletedButUnsyncedPrescripion.uuid)).isEqualTo(deletedButUnsyncedPrescripion)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(prescribedDrugsDao.getOne(deletedPrescription.uuid)).isNull()
    assertThat(prescribedDrugsDao.getOne(notDeletedPrescription.uuid)).isEqualTo(notDeletedPrescription)
    assertThat(prescribedDrugsDao.getOne(deletedButUnsyncedPrescripion.uuid)).isEqualTo(deletedButUnsyncedPrescripion)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_appointments() {
    // given
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("57d2ef99-59e7-4dc5-9cc5-4fe6917386b7"),
        syncStatus = SyncStatus.DONE,
        generateBusinessId = false
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("00001c29-a108-49b7-8db2-e867782c633f"),
        syncStatus = SyncStatus.PENDING,
        generateBusinessId = false
    )

    val deletedAppointment = TestData.appointment(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Scheduled,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val notDeletedAppointment = TestData.appointment(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Scheduled,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val deletedButUnsyncedAppointment = TestData.appointment(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.PENDING,
        status = Appointment.Status.Scheduled,
        patientUuid = notSyncedPatientProfile.patientUuid
    )

    appointmentDao.save(listOf(deletedAppointment, deletedButUnsyncedAppointment, notDeletedAppointment))

    assertThat(appointmentDao.getOne(deletedAppointment.uuid)).isEqualTo(deletedAppointment)
    assertThat(appointmentDao.getOne(notDeletedAppointment.uuid)).isEqualTo(notDeletedAppointment)
    assertThat(appointmentDao.getOne(deletedButUnsyncedAppointment.uuid)).isEqualTo(deletedButUnsyncedAppointment)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(appointmentDao.getOne(deletedAppointment.uuid)).isNull()
    assertThat(appointmentDao.getOne(notDeletedAppointment.uuid)).isEqualTo(notDeletedAppointment)
    assertThat(appointmentDao.getOne(deletedButUnsyncedAppointment.uuid)).isEqualTo(deletedButUnsyncedAppointment)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_medical_histories() {
    // given
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("57d2ef99-59e7-4dc5-9cc5-4fe6917386b7"),
        syncStatus = SyncStatus.DONE,
        generateBusinessId = false
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("00001c29-a108-49b7-8db2-e867782c633f"),
        syncStatus = SyncStatus.PENDING,
        generateBusinessId = false
    )
    val deletedMedicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val notDeletedMedicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val deletedButUnsyncedMedicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.PENDING,
        patientUuid = notSyncedPatientProfile.patientUuid
    )

    medicalHistoryDao.saveHistories(listOf(deletedMedicalHistory, deletedButUnsyncedMedicalHistory, notDeletedMedicalHistory))

    assertThat(medicalHistoryDao.getOne(deletedMedicalHistory.uuid)).isEqualTo(deletedMedicalHistory)
    assertThat(medicalHistoryDao.getOne(notDeletedMedicalHistory.uuid)).isEqualTo(notDeletedMedicalHistory)
    assertThat(medicalHistoryDao.getOne(deletedButUnsyncedMedicalHistory.uuid)).isEqualTo(deletedButUnsyncedMedicalHistory)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(medicalHistoryDao.getOne(deletedMedicalHistory.uuid)).isNull()
    assertThat(medicalHistoryDao.getOne(notDeletedMedicalHistory.uuid)).isEqualTo(notDeletedMedicalHistory)
    assertThat(medicalHistoryDao.getOne(deletedButUnsyncedMedicalHistory.uuid)).isEqualTo(deletedButUnsyncedMedicalHistory)
  }

  @Test
  fun purging_the_database_should_delete_cancelled_and_visited_appointments() {
    // given
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("57d2ef99-59e7-4dc5-9cc5-4fe6917386b7"),
        syncStatus = SyncStatus.DONE,
        generateBusinessId = false
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("00001c29-a108-49b7-8db2-e867782c633f"),
        syncStatus = SyncStatus.PENDING,
        generateBusinessId = false
    )
    val scheduledAppointment = TestData.appointment(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Scheduled,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val cancelledAppointment = TestData.appointment(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Cancelled,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val cancelledButUnsyncedAppointment = TestData.appointment(
        uuid = UUID.fromString("e17b4fed-a2cd-453d-b717-d60ca184892b"),
        deletedAt = null,
        syncStatus = SyncStatus.PENDING,
        status = Appointment.Status.Cancelled,
        patientUuid = notSyncedPatientProfile.patientUuid
    )
    val visitedAppointment = TestData.appointment(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Visited,
        patientUuid = syncedPatientProfile.patientUuid
    )
    val visitedButUnsyncedAppointment = TestData.appointment(
        uuid = UUID.fromString("927b08d3-b19e-4231-8d0b-dcf28e240474"),
        deletedAt = null,
        syncStatus = SyncStatus.PENDING,
        status = Appointment.Status.Visited,
        patientUuid = notSyncedPatientProfile.patientUuid
    )
    val appointmentWithUnknownStatus = TestData.appointment(
        uuid = UUID.fromString("a89a3a34-8395-4a55-89b6-562146369ad1"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Unknown("rescheduled"),
        patientUuid = syncedPatientProfile.patientUuid
    )

    appointmentDao.save(listOf(
        scheduledAppointment,
        visitedAppointment,
        cancelledAppointment,
        visitedButUnsyncedAppointment,
        cancelledButUnsyncedAppointment,
        appointmentWithUnknownStatus
    ))

    assertThat(appointmentDao.getOne(scheduledAppointment.uuid)).isEqualTo(scheduledAppointment)
    assertThat(appointmentDao.getOne(cancelledAppointment.uuid)).isEqualTo(cancelledAppointment)
    assertThat(appointmentDao.getOne(cancelledButUnsyncedAppointment.uuid)).isEqualTo(cancelledButUnsyncedAppointment)
    assertThat(appointmentDao.getOne(visitedAppointment.uuid)).isEqualTo(visitedAppointment)
    assertThat(appointmentDao.getOne(visitedButUnsyncedAppointment.uuid)).isEqualTo(visitedButUnsyncedAppointment)
    assertThat(appointmentDao.getOne(appointmentWithUnknownStatus.uuid)).isEqualTo(appointmentWithUnknownStatus)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(appointmentDao.getOne(scheduledAppointment.uuid)).isEqualTo(scheduledAppointment)
    assertThat(appointmentDao.getOne(cancelledAppointment.uuid)).isNull()
    assertThat(appointmentDao.getOne(visitedAppointment.uuid)).isNull()
    assertThat(appointmentDao.getOne(cancelledButUnsyncedAppointment.uuid)).isEqualTo(cancelledButUnsyncedAppointment)
    assertThat(appointmentDao.getOne(visitedButUnsyncedAppointment.uuid)).isEqualTo(visitedButUnsyncedAppointment)
    assertThat(appointmentDao.getOne(appointmentWithUnknownStatus.uuid)).isEqualTo(appointmentWithUnknownStatus)
  }

  @Test
  fun purging_the_database_should_hard_delete_patients_when_retention_time_has_passed() {
    // given
    val patientWithPassedRetentionTime = TestData.patientProfile(
        patientUuid = UUID.fromString("c3c84410-02f5-430e-a067-1830683a4d27"),
        patientAddressUuid = UUID.fromString("a5b8a711-a9e0-4474-8b9f-3b6193ec6001"),
        syncStatus = SyncStatus.DONE,
        generatePhoneNumber = true,
        generateBusinessId = true,
        patientDeletedAt = Instant.parse("2021-02-21T00:00:00Z"),
        retainUntil = Instant.parse("2021-05-21T00:00:00Z")
    )

    val patientWithNullRetentionTime = TestData.patientProfile(
        patientUuid = UUID.fromString("6f8ae9d8-522e-4eed-8a0d-4be393669945"),
        patientAddressUuid = UUID.fromString("8e0f0203-b7e3-4d4a-91be-3bd3385a7ee5"),
        syncStatus = SyncStatus.DONE,
        generatePhoneNumber = true,
        generateBusinessId = false,
        retainUntil = null
    )

    val patientWithNotPassedRetentionTime = TestData.patientProfile(
        patientUuid = UUID.fromString("afaea7ee-22cc-4a27-9906-309a377f83be"),
        syncStatus = SyncStatus.PENDING,
        generatePhoneNumber = true,
        generateBusinessId = true,
        retainUntil = Instant.parse("2022-01-05T00:00:00Z"),
    )

    patientAddressDao.save(listOf(patientWithPassedRetentionTime.address, patientWithNotPassedRetentionTime.address, patientWithNullRetentionTime.address))
    patientDao.save(listOf(patientWithPassedRetentionTime.patient, patientWithNotPassedRetentionTime.patient, patientWithNullRetentionTime.patient))
    phoneNumberDao.save(patientWithPassedRetentionTime.phoneNumbers + patientWithNotPassedRetentionTime.phoneNumbers + patientWithNullRetentionTime.phoneNumbers)
    businessIdDao.save(patientWithPassedRetentionTime.businessIds + patientWithNotPassedRetentionTime.businessIds + patientWithNullRetentionTime.businessIds)

    assertThat(patientDao.patientProfileImmediate(patientWithPassedRetentionTime.patientUuid)).isEqualTo(patientWithPassedRetentionTime)
    assertThat(patientDao.patientProfileImmediate(patientWithNotPassedRetentionTime.patientUuid)).isEqualTo(patientWithNotPassedRetentionTime)
    assertThat(patientDao.patientProfileImmediate(patientWithNullRetentionTime.patientUuid)).isEqualTo(patientWithNullRetentionTime)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(patientDao.patientProfileImmediate(patientWithPassedRetentionTime.patientUuid)).isNull()
    assertThat(patientDao.patientProfileImmediate(patientWithNotPassedRetentionTime.patientUuid)).isEqualTo(patientWithNotPassedRetentionTime)
    assertThat(patientDao.patientProfileImmediate(patientWithNullRetentionTime.patientUuid)).isEqualTo(patientWithNullRetentionTime)
  }

  @Test
  fun purging_the_database_should_hard_delete_blood_pressure_measurements_when_patient_is_null() {
    // given
    val deletedPatientUuid = UUID.fromString("1f86c321-5539-44f8-8708-3bcc0e44feed")
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("73f22e49-669e-4959-843e-7684622c4184"),
        syncStatus = SyncStatus.DONE,
        generateBusinessId = false
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("ee60db3e-9ecb-49aa-829c-474f707b3739"),
        syncStatus = SyncStatus.PENDING,
        generateBusinessId = false
    )
    val deletedBloodPressureMeasurement = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("aa2c81ce-236c-4e4a-9ebd-f88a31afb15d"),
        patientUuid = deletedPatientUuid,
        syncStatus = SyncStatus.DONE
    )
    val notDeletedBloodPressureMeasurement = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("34ab2f90-9332-4743-925e-3088a23a933d"),
        patientUuid = syncedPatientProfile.patientUuid,
        syncStatus = SyncStatus.DONE,
        deletedAt = null
    )

    patientAddressDao.save(listOf(syncedPatientProfile.address, notSyncedPatientProfile.address))
    patientDao.save(listOf(syncedPatientProfile.patient, notSyncedPatientProfile.patient))
    bloodPressureDao.save(listOf(deletedBloodPressureMeasurement, notDeletedBloodPressureMeasurement))

    assertThat(bloodPressureDao.getOne(deletedBloodPressureMeasurement.uuid)).isEqualTo(deletedBloodPressureMeasurement)
    assertThat(bloodPressureDao.getOne(notDeletedBloodPressureMeasurement.uuid)).isEqualTo(notDeletedBloodPressureMeasurement)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(bloodPressureDao.getOne(deletedBloodPressureMeasurement.uuid)).isNull()
    assertThat(bloodPressureDao.getOne(notDeletedBloodPressureMeasurement.uuid)).isEqualTo(notDeletedBloodPressureMeasurement)
  }

  @Test
  fun purging_the_database_should_hard_delete_blood_sugar_measurements_when_patient_is_null() {
    // given
    val deletedPatientUuid = UUID.fromString("1f86c321-5539-44f8-8708-3bcc0e44feed")
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("b5ae0427-4513-41f9-87ad-127166f35916"),
        syncStatus = SyncStatus.DONE
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("1bd41323-9abc-4c3a-908f-6902a19fe26b"),
        syncStatus = SyncStatus.PENDING
    )
    val deletedBloodSugarMeasurement = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("d6530f41-c4be-455c-b5c3-c388e451d3c4"),
        patientUuid = deletedPatientUuid,
        deletedAt = Instant.parse("2021-07-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE
    )
    val notDeletedBloodSugarMeasurement = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("489b6439-b424-487e-a01a-7ebd48628421"),
        patientUuid = syncedPatientProfile.patientUuid,
        syncStatus = SyncStatus.PENDING
    )

    patientAddressDao.save(listOf(syncedPatientProfile.address, notSyncedPatientProfile.address))
    patientDao.save(listOf(syncedPatientProfile.patient, notSyncedPatientProfile.patient))
    bloodSugarDao.save(listOf(deletedBloodSugarMeasurement, notDeletedBloodSugarMeasurement))

    assertThat(bloodSugarDao.getOne(deletedBloodSugarMeasurement.uuid)).isEqualTo(deletedBloodSugarMeasurement)
    assertThat(bloodSugarDao.getOne(notDeletedBloodSugarMeasurement.uuid)).isEqualTo(notDeletedBloodSugarMeasurement)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(bloodSugarDao.getOne(deletedBloodSugarMeasurement.uuid)).isNull()
    assertThat(bloodSugarDao.getOne(notDeletedBloodSugarMeasurement.uuid)).isEqualTo(notDeletedBloodSugarMeasurement)
  }

  @Test
  fun purging_the_database_should_hard_delete_appointments_when_patient_is_null() {
    // given
    val deletedPatientUuid = UUID.fromString("1f86c321-5539-44f8-8708-3bcc0e44feed")
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("4913a16c-9ae8-4f20-9c0b-f13c8b61d8ed"),
        syncStatus = SyncStatus.DONE
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("a96e1db3-e958-44f7-8d15-b4fed44d3cf6"),
        syncStatus = SyncStatus.PENDING
    )
    val deletedAppointment = TestData.appointment(
        uuid = UUID.fromString("efdbcafb-91ac-4902-854f-3a2f61af77f8"),
        patientUuid = deletedPatientUuid,
        deletedAt = Instant.parse("2021-07-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE
    )
    val notDeletedAppointment = TestData.appointment(
        uuid = UUID.fromString("dd7b94dd-5dc7-465b-a040-ca5e14571274"),
        patientUuid = syncedPatientProfile.patientUuid,
        syncStatus = SyncStatus.PENDING
    )

    patientAddressDao.save(listOf(syncedPatientProfile.address, notSyncedPatientProfile.address))
    patientDao.save(listOf(syncedPatientProfile.patient, notSyncedPatientProfile.patient))
    appointmentDao.save(listOf(deletedAppointment, notDeletedAppointment))

    assertThat(appointmentDao.getOne(deletedAppointment.uuid)).isEqualTo(deletedAppointment)
    assertThat(appointmentDao.getOne(notDeletedAppointment.uuid)).isEqualTo(notDeletedAppointment)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(appointmentDao.getOne(deletedAppointment.uuid)).isNull()
    assertThat(appointmentDao.getOne(notDeletedAppointment.uuid)).isEqualTo(notDeletedAppointment)
  }

  @Test
  fun purging_the_database_should_hard_delete_medical_history_when_patient_is_null() {
    // given
    val deletedPatientUuid = UUID.fromString("1f86c321-5539-44f8-8708-3bcc0e44feed")
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("baab44dc-84df-4fa5-b445-14e8ab2d5488"),
        syncStatus = SyncStatus.DONE
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("1a758240-fe2e-4a93-a84f-c7481e09aff6"),
        syncStatus = SyncStatus.PENDING
    )
    val deletedMedicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("04ad54d5-e561-4629-ab3f-d4d6cd7a7500"),
        patientUuid = deletedPatientUuid,
        deletedAt = Instant.parse("2021-07-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE
    )
    val notDeletedMedicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("45497481-17d0-4bac-9dd6-2656258b957a"),
        patientUuid = syncedPatientProfile.patientUuid,
        syncStatus = SyncStatus.PENDING
    )

    patientAddressDao.save(listOf(syncedPatientProfile.address, notSyncedPatientProfile.address))
    patientDao.save(listOf(syncedPatientProfile.patient, notSyncedPatientProfile.patient))
    medicalHistoryDao.saveHistories(listOf(deletedMedicalHistory, notDeletedMedicalHistory))

    assertThat(medicalHistoryDao.getOne(deletedMedicalHistory.uuid)).isEqualTo(deletedMedicalHistory)
    assertThat(medicalHistoryDao.getOne(notDeletedMedicalHistory.uuid)).isEqualTo(notDeletedMedicalHistory)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(medicalHistoryDao.getOne(deletedMedicalHistory.uuid)).isNull()
    assertThat(medicalHistoryDao.getOne(notDeletedMedicalHistory.uuid)).isEqualTo(notDeletedMedicalHistory)
  }

  @Test
  fun purging_the_database_should_hard_delete_prescribed_drug_when_patient_is_null() {
    // given
    val deletedPatientUuid = UUID.fromString("1f86c321-5539-44f8-8708-3bcc0e44feed")
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("de68afab-4a8b-449a-a2b5-34cce216c5c8"),
        syncStatus = SyncStatus.DONE
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("a0af7179-e214-486f-96a7-273bb9492390"),
        syncStatus = SyncStatus.PENDING
    )
    val deletedPrescribedDrug = TestData.prescription(
        uuid = UUID.fromString("f96ca4e2-75d6-4de4-b3ff-84dbaeb61c0a"),
        patientUuid = deletedPatientUuid,
        deletedAt = Instant.parse("2021-06-04T00:00:00Z"),
        syncStatus = SyncStatus.DONE
    )
    val notDeletedPrescribedDrug = TestData.prescription(
        uuid = UUID.fromString("b7d1280a-535f-4342-89ef-e2ec7b639da3"),
        patientUuid = syncedPatientProfile.patientUuid,
        syncStatus = SyncStatus.PENDING
    )

    patientAddressDao.save(listOf(syncedPatientProfile.address, notSyncedPatientProfile.address))
    patientDao.save(listOf(syncedPatientProfile.patient, notSyncedPatientProfile.patient))
    prescribedDrugsDao.save(listOf(deletedPrescribedDrug, notDeletedPrescribedDrug))

    assertThat(prescribedDrugsDao.getOne(deletedPrescribedDrug.uuid)).isEqualTo(deletedPrescribedDrug)
    assertThat(prescribedDrugsDao.getOne(notDeletedPrescribedDrug.uuid)).isEqualTo(notDeletedPrescribedDrug)

    // when
    appDatabase.purge(Instant.parse("2021-06-01T00:00:00Z"))

    // then
    assertThat(prescribedDrugsDao.getOne(deletedPrescribedDrug.uuid)).isNull()
    assertThat(prescribedDrugsDao.getOne(notDeletedPrescribedDrug.uuid)).isEqualTo(notDeletedPrescribedDrug)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_call_results() {
    // given
    val deletedCallResult = TestData.callResult(
        id = UUID.fromString("57e5bc23-0865-48d7-8e3b-34b111b318f2"),
        deletedAt = Instant.parse("2018-01-01T00:00:02Z"),
        syncStatus = SyncStatus.DONE
    )
    val notDeletedCallResult = TestData.callResult(
        id = UUID.fromString("2bd34f85-052e-4c1b-a110-b7a92097762a"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE
    )
    val deletedButUnsyncedCallResult = TestData.callResult(
        id = UUID.fromString("b924feac-72de-48dc-93d5-74c4bd774a0f"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.PENDING
    )

    callResultDao.save(listOf(deletedCallResult, notDeletedCallResult, deletedButUnsyncedCallResult))

    assertThat(callResultDao.getOne(deletedCallResult.id)).isEqualTo(deletedCallResult)
    assertThat(callResultDao.getOne(notDeletedCallResult.id)).isEqualTo(notDeletedCallResult)
    assertThat(callResultDao.getOne(deletedButUnsyncedCallResult.id)).isEqualTo(deletedButUnsyncedCallResult)

    // when
    appDatabase.purge(Instant.parse("2018-01-01T00:00:00Z"))

    // then
    assertThat(callResultDao.getOne(deletedCallResult.id)).isNull()
    assertThat(callResultDao.getOne(notDeletedCallResult.id)).isEqualTo(notDeletedCallResult)
    assertThat(callResultDao.getOne(deletedButUnsyncedCallResult.id)).isEqualTo(deletedButUnsyncedCallResult)
  }
}
