package org.simple.clinic.storage

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
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

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_patients() {
    // given
    val deletedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("2463850b-c294-4db4-906b-68d9ea8e99a1"),
        generatePhoneNumber = true,
        generateBusinessId = true,
        patientDeletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE
    )
    val notDeletedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("427bfcf1-1b6c-42fa-903b-8527c594a0f9"),
        generatePhoneNumber = true,
        generateBusinessId = true,
        syncStatus = SyncStatus.DONE,
        patientDeletedAt = null
    )
    val deletedButUnsyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("3f31b541-afae-41a3-9eb3-6e269ad6fb5d"),
        generatePhoneNumber = true,
        generateBusinessId = true,
        patientDeletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.PENDING
    )
    patientAddressDao.save(listOf(deletedPatientProfile.address, notDeletedPatientProfile.address, deletedButUnsyncedPatientProfile.address))
    patientDao.save(listOf(deletedPatientProfile.patient, notDeletedPatientProfile.patient, deletedButUnsyncedPatientProfile.patient))
    phoneNumberDao.save(deletedPatientProfile.phoneNumbers + notDeletedPatientProfile.phoneNumbers + deletedButUnsyncedPatientProfile.phoneNumbers)
    businessIdDao.save(deletedPatientProfile.businessIds + notDeletedPatientProfile.businessIds + deletedButUnsyncedPatientProfile.businessIds)

    assertThat(patientDao.patientProfileImmediate(deletedPatientProfile.patientUuid)).isEqualTo(deletedPatientProfile)
    assertThat(patientDao.patientProfileImmediate(notDeletedPatientProfile.patientUuid)).isEqualTo(notDeletedPatientProfile)
    assertThat(patientDao.patientProfileImmediate(deletedButUnsyncedPatientProfile.patientUuid)).isEqualTo(deletedButUnsyncedPatientProfile)

    // when
    appDatabase.purge()

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
        generatePhoneNumber = false,
        syncStatus = SyncStatus.DONE
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("00001c29-a108-49b7-8db2-e867782c633f"),
        generatePhoneNumber = false,
        syncStatus = SyncStatus.PENDING
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
    appDatabase.purge()

    // then
    assertThat(phoneNumberDao.phoneNumber(syncedPatientProfile.patientUuid).blockingFirst()).containsExactly(notDeletedPhoneNumber)
    assertThat(phoneNumberDao.phoneNumber(notSyncedPatientProfile.patientUuid).blockingFirst()).containsExactly(deletedButUnsyncedPhoneNumber)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_business_ids() {
    // given
    val syncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("57d2ef99-59e7-4dc5-9cc5-4fe6917386b7"),
        generateBusinessId = false,
        syncStatus = SyncStatus.DONE
    )
    val notSyncedPatientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("00001c29-a108-49b7-8db2-e867782c633f"),
        generateBusinessId = false,
        syncStatus = SyncStatus.PENDING
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
    appDatabase.purge()

    // then
    assertThat(businessIdDao.get(deletedBusinessId.uuid)).isNull()
    assertThat(businessIdDao.get(notDeletedBusinessId.uuid)).isEqualTo(notDeletedBusinessId)
    assertThat(businessIdDao.get(deletedButUnsyncedBusinessId.uuid)).isEqualTo(deletedButUnsyncedBusinessId)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_blood_pressure_measurements() {
    // given
    val deletedBloodPressureMeasurement = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE
    )
    val notDeletedBloodPressureMeasurement = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE
    )
    val deletedButUnsyncedBloodPressureMeasurement = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.PENDING
    )

    bloodPressureDao.save(listOf(deletedBloodPressureMeasurement, deletedButUnsyncedBloodPressureMeasurement, notDeletedBloodPressureMeasurement))

    assertThat(bloodPressureDao.getOne(deletedBloodPressureMeasurement.uuid)).isEqualTo(deletedBloodPressureMeasurement)
    assertThat(bloodPressureDao.getOne(notDeletedBloodPressureMeasurement.uuid)).isEqualTo(notDeletedBloodPressureMeasurement)
    assertThat(bloodPressureDao.getOne(deletedButUnsyncedBloodPressureMeasurement.uuid)).isEqualTo(deletedButUnsyncedBloodPressureMeasurement)

    // when
    appDatabase.purge()

    // then
    assertThat(bloodPressureDao.getOne(deletedBloodPressureMeasurement.uuid)).isNull()
    assertThat(bloodPressureDao.getOne(notDeletedBloodPressureMeasurement.uuid)).isEqualTo(notDeletedBloodPressureMeasurement)
    assertThat(bloodPressureDao.getOne(deletedButUnsyncedBloodPressureMeasurement.uuid)).isEqualTo(deletedButUnsyncedBloodPressureMeasurement)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_blood_sugar_measurements() {
    // given
    val deletedBloodSugarMeasurement = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE
    )
    val notDeletedBloodSugarMeasurement = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE
    )
    val deletedButUnsyncedBloodSugarMeasurement = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.PENDING
    )

    bloodSugarDao.save(listOf(deletedBloodSugarMeasurement, deletedButUnsyncedBloodSugarMeasurement, notDeletedBloodSugarMeasurement))

    assertThat(bloodSugarDao.getOne(deletedBloodSugarMeasurement.uuid)).isEqualTo(deletedBloodSugarMeasurement)
    assertThat(bloodSugarDao.getOne(notDeletedBloodSugarMeasurement.uuid)).isEqualTo(notDeletedBloodSugarMeasurement)
    assertThat(bloodSugarDao.getOne(deletedButUnsyncedBloodSugarMeasurement.uuid)).isEqualTo(deletedButUnsyncedBloodSugarMeasurement)

    // when
    appDatabase.purge()

    // then
    assertThat(bloodSugarDao.getOne(deletedBloodSugarMeasurement.uuid)).isNull()
    assertThat(bloodSugarDao.getOne(notDeletedBloodSugarMeasurement.uuid)).isEqualTo(notDeletedBloodSugarMeasurement)
    assertThat(bloodSugarDao.getOne(deletedButUnsyncedBloodSugarMeasurement.uuid)).isEqualTo(deletedButUnsyncedBloodSugarMeasurement)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_prescriptions() {
    // given
    val deletedPrescription = TestData.prescription(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        isDeleted = true,
        syncStatus = SyncStatus.DONE
    )
    val notDeletedPrescription = TestData.prescription(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        isDeleted = false,
        syncStatus = SyncStatus.DONE
    )
    val deletedButUnsyncedPrescripion = TestData.prescription(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        isDeleted = true,
        syncStatus = SyncStatus.PENDING
    )

    prescribedDrugsDao.save(listOf(deletedPrescription, deletedButUnsyncedPrescripion, notDeletedPrescription))

    assertThat(prescribedDrugsDao.getOne(deletedPrescription.uuid)).isEqualTo(deletedPrescription)
    assertThat(prescribedDrugsDao.getOne(notDeletedPrescription.uuid)).isEqualTo(notDeletedPrescription)
    assertThat(prescribedDrugsDao.getOne(deletedButUnsyncedPrescripion.uuid)).isEqualTo(deletedButUnsyncedPrescripion)

    // when
    appDatabase.purge()

    // then
    assertThat(prescribedDrugsDao.getOne(deletedPrescription.uuid)).isNull()
    assertThat(prescribedDrugsDao.getOne(notDeletedPrescription.uuid)).isEqualTo(notDeletedPrescription)
    assertThat(prescribedDrugsDao.getOne(deletedButUnsyncedPrescripion.uuid)).isEqualTo(deletedButUnsyncedPrescripion)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_appointments() {
    // given
    val deletedAppointment = TestData.appointment(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Scheduled
    )
    val notDeletedAppointment = TestData.appointment(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Scheduled
    )
    val deletedButUnsyncedAppointment = TestData.appointment(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.PENDING,
        status = Appointment.Status.Scheduled
    )

    appointmentDao.save(listOf(deletedAppointment, deletedButUnsyncedAppointment, notDeletedAppointment))

    assertThat(appointmentDao.getOne(deletedAppointment.uuid)).isEqualTo(deletedAppointment)
    assertThat(appointmentDao.getOne(notDeletedAppointment.uuid)).isEqualTo(notDeletedAppointment)
    assertThat(appointmentDao.getOne(deletedButUnsyncedAppointment.uuid)).isEqualTo(deletedButUnsyncedAppointment)

    // when
    appDatabase.purge()

    // then
    assertThat(appointmentDao.getOne(deletedAppointment.uuid)).isNull()
    assertThat(appointmentDao.getOne(notDeletedAppointment.uuid)).isEqualTo(notDeletedAppointment)
    assertThat(appointmentDao.getOne(deletedButUnsyncedAppointment.uuid)).isEqualTo(deletedButUnsyncedAppointment)
  }

  @Test
  fun purging_the_database_should_delete_soft_deleted_medical_histories() {
    // given
    val deletedMedicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.DONE
    )
    val notDeletedMedicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE
    )
    val deletedButUnsyncedMedicalHistory = TestData.medicalHistory(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        deletedAt = Instant.parse("2018-01-01T00:00:00Z"),
        syncStatus = SyncStatus.PENDING
    )

    medicalHistoryDao.save(listOf(deletedMedicalHistory, deletedButUnsyncedMedicalHistory, notDeletedMedicalHistory))

    assertThat(medicalHistoryDao.getOne(deletedMedicalHistory.uuid)).isEqualTo(deletedMedicalHistory)
    assertThat(medicalHistoryDao.getOne(notDeletedMedicalHistory.uuid)).isEqualTo(notDeletedMedicalHistory)
    assertThat(medicalHistoryDao.getOne(deletedButUnsyncedMedicalHistory.uuid)).isEqualTo(deletedButUnsyncedMedicalHistory)

    // when
    appDatabase.purge()

    // then
    assertThat(medicalHistoryDao.getOne(deletedMedicalHistory.uuid)).isNull()
    assertThat(medicalHistoryDao.getOne(notDeletedMedicalHistory.uuid)).isEqualTo(notDeletedMedicalHistory)
    assertThat(medicalHistoryDao.getOne(deletedButUnsyncedMedicalHistory.uuid)).isEqualTo(deletedButUnsyncedMedicalHistory)
  }

  @Test
  fun purging_the_database_should_delete_cancelled_and_visited_appointments() {
    // given
    val scheduledAppointment = TestData.appointment(
        uuid = UUID.fromString("26170a3e-e04e-4488-9893-30e7e5463e0e"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Scheduled
    )
    val cancelledAppointment = TestData.appointment(
        uuid = UUID.fromString("25492f9e-865d-4296-ab31-e5cc6141cd58"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Cancelled
    )
    val cancelledButUnsyncedAppointment = TestData.appointment(
        uuid = UUID.fromString("e17b4fed-a2cd-453d-b717-d60ca184892b"),
        deletedAt = null,
        syncStatus = SyncStatus.PENDING,
        status = Appointment.Status.Cancelled
    )
    val visitedAppointment = TestData.appointment(
        uuid = UUID.fromString("13333a77-f20d-4b96-9c11-c0b38ae99ce5"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Visited
    )
    val visitedButUnsyncedAppointment = TestData.appointment(
        uuid = UUID.fromString("927b08d3-b19e-4231-8d0b-dcf28e240474"),
        deletedAt = null,
        syncStatus = SyncStatus.PENDING,
        status = Appointment.Status.Visited
    )
    val appointmentWithUnknownStatus = TestData.appointment(
        uuid = UUID.fromString("a89a3a34-8395-4a55-89b6-562146369ad1"),
        deletedAt = null,
        syncStatus = SyncStatus.DONE,
        status = Appointment.Status.Unknown("rescheduled")
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
    appDatabase.purge()

    // then
    assertThat(appointmentDao.getOne(scheduledAppointment.uuid)).isEqualTo(scheduledAppointment)
    assertThat(appointmentDao.getOne(cancelledAppointment.uuid)).isNull()
    assertThat(appointmentDao.getOne(visitedAppointment.uuid)).isNull()
    assertThat(appointmentDao.getOne(cancelledButUnsyncedAppointment.uuid)).isEqualTo(cancelledButUnsyncedAppointment)
    assertThat(appointmentDao.getOne(visitedButUnsyncedAppointment.uuid)).isEqualTo(visitedButUnsyncedAppointment)
    assertThat(appointmentDao.getOne(appointmentWithUnknownStatus.uuid)).isEqualTo(appointmentWithUnknownStatus)
  }
}
