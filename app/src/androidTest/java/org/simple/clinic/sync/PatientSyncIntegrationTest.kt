package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.patient.sync.PatientSyncApi
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import org.simple.clinic.util.Rules
import org.simple.clinic.util.unsafeLazy
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class PatientSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var repository: PatientRepository

  @Inject
  @Named("last_patient_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: PatientSyncApi

  @Inject
  lateinit var userSession: UserSession

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  private lateinit var patientSync: PatientSync

  private val batchSize = 3
  private val config = SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = batchSize,
      syncGroup = SyncGroup.FREQUENT
  )

  private val currentFacilityUuid: UUID by unsafeLazy { userSession.loggedInUserImmediate()!!.currentFacilityUuid }

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    resetLocalData()

    patientSync = PatientSync(
        syncCoordinator = SyncCoordinator(),
        repository = repository,
        api = syncApi,
        userSession = userSession,
        lastPullToken = lastPullToken,
        config = config
    )
  }

  @After
  fun tearDown() {
    resetLocalData()
  }

  private fun resetLocalData() {
    clearPatientData()
    lastPullToken.delete()
  }

  private fun clearPatientData() {
    appDatabase.patientDao().clear()
    appDatabase.addressDao().clear()
    appDatabase.phoneNumberDao().clear()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // given
    val totalNumberOfRecords = batchSize * 2 + 1
    val records = (1..totalNumberOfRecords).map {
      TestData.patientProfile(
          syncStatus = SyncStatus.PENDING,
          patientRegisteredFacilityId = currentFacilityUuid,
          patientAssignedFacilityId = currentFacilityUuid
      )
    }
    assertThat(records).containsNoDuplicates()

    repository.save(records).blockingAwait()
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(totalNumberOfRecords)

    // when
    patientSync.push().blockingAwait()
    clearPatientData()
    patientSync.pull().blockingAwait()

    // then
    val expectedPulledRecords = records.map { it.syncCompleted() }
    val pulledRecords = repository.recordsWithSyncStatus(SyncStatus.DONE).blockingGet()

    assertThat(pulledRecords).containsAtLeastElementsIn(expectedPulledRecords)
  }

  @Test
  fun sync_pending_records_should_not_be_overwritten_by_server_records() {
    // given
    val records = (1..batchSize).map {
      TestData.patientProfile(
          syncStatus = SyncStatus.PENDING,
          patientRegisteredFacilityId = currentFacilityUuid,
          patientAssignedFacilityId = currentFacilityUuid
      )
    }
    assertThat(records).containsNoDuplicates()

    repository.save(records).blockingAwait()
    patientSync.push().blockingAwait()
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(0)

    val modifiedRecord = records[1].withName("Anish Acharya")
    repository.save(listOf(modifiedRecord)).blockingAwait()
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(1)

    // when
    patientSync.pull().blockingAwait()

    // then
    val expectedSavedRecords = records
        .map { it.syncCompleted() }
        .filterNot { it.patientUuid == modifiedRecord.patientUuid }

    val savedRecords = repository.recordsWithSyncStatus(SyncStatus.DONE).blockingGet()
    val pendingSyncRecords = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()

    assertThat(savedRecords).containsAtLeastElementsIn(expectedSavedRecords)
    assertThat(pendingSyncRecords).containsExactly(modifiedRecord)
  }

  private fun PatientProfile.syncCompleted(): PatientProfile = copy(patient = patient.copy(syncStatus = SyncStatus.DONE))

  private fun PatientProfile.withName(name: String): PatientProfile {
    val updatedPatient = patient.copy(
        fullName = name,
        syncStatus = SyncStatus.PENDING,
        updatedAt = patient.updatedAt.plusMillis(1)
    )
    return copy(patient = updatedPatient)
  }
}
