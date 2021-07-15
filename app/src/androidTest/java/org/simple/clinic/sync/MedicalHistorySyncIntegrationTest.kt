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
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.medicalhistory.sync.MedicalHistorySyncApi
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.RegisterPatientRule
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Rules
import java.util.Optional
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class MedicalHistorySyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var repository: MedicalHistoryRepository

  @Inject
  @Named("last_medicalhistory_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: MedicalHistorySyncApi

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var syncInterval: SyncInterval

  private val patientUuid = UUID.fromString("a635445b-16aa-49cb-af4b-fab766a5f11e")

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())
      // Needed because the server only syncs resources if a patient exists
      .around(RegisterPatientRule(patientUuid))

  private lateinit var sync: MedicalHistorySync

  private val batchSize = 3
  private lateinit var config: SyncConfig

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    resetLocalData()

    config = SyncConfig(
        syncInterval = syncInterval,
        pullBatchSize = batchSize,
        pushBatchSize = batchSize,
        syncGroup = SyncGroup.FREQUENT
    )

    sync = MedicalHistorySync(
        syncCoordinator = SyncCoordinator(),
        repository = repository,
        api = syncApi,
        lastPullToken = lastPullToken,
        config = config
    )
  }

  @After
  fun tearDown() {
    resetLocalData()
  }

  private fun resetLocalData() {
    clearMedicalHistoryData()
    lastPullToken.delete()
  }

  private fun clearMedicalHistoryData() {
    appDatabase.medicalHistoryDao().clear()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // given
    val totalNumberOfRecords = batchSize * 2 + 1
    val records = (1..totalNumberOfRecords).map {
      TestData.medicalHistory(
          syncStatus = SyncStatus.PENDING,
          patientUuid = patientUuid
      )
    }
    assertThat(records).containsNoDuplicates()

    repository.save(records).blockingAwait()
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(totalNumberOfRecords)

    // when
    sync.push()
    clearMedicalHistoryData()
    sync.pull()

    // then
    val expectedPulledRecords = records.map { it.syncCompleted() }
    val pulledRecords = repository.recordsWithSyncStatus(SyncStatus.DONE)

    assertThat(pulledRecords).containsAtLeastElementsIn(expectedPulledRecords)
  }

  @Test
  fun sync_pending_records_should_not_be_overwritten_by_server_records() {
    // given
    val records = (1..batchSize).map {
      TestData.medicalHistory(
          syncStatus = SyncStatus.PENDING,
          patientUuid = patientUuid
      )
    }
    assertThat(records).containsNoDuplicates()

    repository.save(records).blockingAwait()
    sync.push()
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(0)

    val modifiedRecord = records[1].hadHeartAttack()
    repository.save(listOf(modifiedRecord)).blockingAwait()
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(1)

    // when
    sync.pull()

    // then
    val expectedSavedRecords = records
        .map { it.syncCompleted() }
        .filterNot { it.patientUuid == modifiedRecord.patientUuid }

    val savedRecords = repository.recordsWithSyncStatus(SyncStatus.DONE)
    val pendingSyncRecords = repository.recordsWithSyncStatus(SyncStatus.PENDING)

    assertThat(savedRecords).containsAtLeastElementsIn(expectedSavedRecords)
    assertThat(pendingSyncRecords).containsExactly(modifiedRecord)
  }

  private fun MedicalHistory.syncCompleted(): MedicalHistory = copy(syncStatus = SyncStatus.DONE)

  private fun MedicalHistory.hadHeartAttack(): MedicalHistory {
    return copy(
        hasHadHeartAttack = Answer.Yes,
        syncStatus = SyncStatus.PENDING,
        updatedAt = updatedAt.plusMillis(1)
    )
  }
}
