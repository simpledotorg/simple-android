package org.simple.clinic.storage

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
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
}
