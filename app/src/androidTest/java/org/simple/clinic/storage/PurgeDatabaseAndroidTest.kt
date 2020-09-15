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
}
