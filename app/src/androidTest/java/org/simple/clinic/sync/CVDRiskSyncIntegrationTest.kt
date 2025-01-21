package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.cvdrisk.CVDRisk
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.CVDRiskRepository
import org.simple.clinic.cvdrisk.sync.CVDRiskSync
import org.simple.clinic.cvdrisk.sync.CVDRiskSyncApi
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastCVDRiskPullToken
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.RegisterPatientRule
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.sharedTestCode.TestData
import org.simple.clinic.util.Rules
import java.util.Optional
import java.util.UUID
import javax.inject.Inject

class CVDRiskSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var repository: CVDRiskRepository

  @Inject
  @TypedPreference(LastCVDRiskPullToken)
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: CVDRiskSyncApi

  @Inject
  lateinit var syncInterval: SyncInterval

  private val patientUuid = UUID.fromString("a524f7ed-e088-407f-951f-f9632f801a7c")

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())
      .around(RegisterPatientRule(patientUuid))
      .around(SaveDatabaseRule())

  private lateinit var sync: CVDRiskSync

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
        name = ""
    )

    sync = CVDRiskSync(
        syncCoordinator = SyncCoordinator(),
        repository = repository,
        api = syncApi,
        lastPullToken = lastPullToken,
        config = config
    )
  }

  private fun resetLocalData() {
    clearData()
    lastPullToken.delete()
  }

  private fun clearData() {
    appDatabase.patientAttributeDao().clear()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // given
    val totalNumberOfRecords = batchSize * 2 + 1
    val records = (1..totalNumberOfRecords).map {
      TestData.cvdRisk(
          patientUuid = patientUuid,
          riskScore = CVDRiskRange(17, 17),
          syncStatus = SyncStatus.PENDING,
      )
    }
    assertThat(records).containsNoDuplicates()

    repository.save(records)
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(totalNumberOfRecords)

    // when
    sync.push()
    clearData()
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
      TestData.cvdRisk(
          patientUuid = patientUuid,
          riskScore = CVDRiskRange(17, 17),
          syncStatus = SyncStatus.PENDING,
      )
    }
    assertThat(records).containsNoDuplicates()

    repository.save(records)
    sync.push()
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(0)

    val modifiedRecord = records[1].withScore(CVDRiskRange(21, 21))
    repository.save(listOf(modifiedRecord))
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

  private fun CVDRisk.syncCompleted(): CVDRisk = copy(syncStatus = SyncStatus.DONE)

  private fun CVDRisk.withScore(riskScore: CVDRiskRange): CVDRisk {
    return copy(
        riskScore = riskScore,
        syncStatus = SyncStatus.PENDING,
        timestamps = timestamps.copy(updatedAt = timestamps.updatedAt.plusMillis(1))
    )
  }
}
