package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.util.Rules
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@Ignore("API endpoints are not yet ready")
class TeleconsultRecordSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var teleconsultRecordApi: TeleconsultRecordApi

  @Inject
  lateinit var repository: TeleconsultRecordRepository

  private lateinit var teleconsultRecordSync: TeleconsultRecordSync

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  private val batchSize = 0
  private val config = SyncConfig(
      syncInterval = SyncInterval.DAILY,
      batchSize = batchSize,
      syncGroup = SyncGroup.DAILY
  )

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    resetLocalData()

    teleconsultRecordSync = TeleconsultRecordSync(
        syncCoordinator = SyncCoordinator(),
        teleconsultRecordApi = teleconsultRecordApi,
        teleconsultRecordRepository = repository,
        config = config
    )
  }

  @After
  fun tearDown() {
    resetLocalData()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // given
    val totalNumberOfRecords = batchSize * 2 + 1
    val teleconsultRecordId = UUID.fromString("fa4e8fba-2a88-49ad-b960-e8cd96935fb9")
    val records = (1..totalNumberOfRecords).map {
      TestData.teleconsultRecord(
          id = teleconsultRecordId,
          patientId = UUID.fromString("92974564-9194-4a04-b4bd-5ac1fba82e31"),
          medicalOfficerId = UUID.fromString("39f4307b-0aef-49d4-9578-371524cd0212"),
          teleconsultRequestInfo = null,
          teleconsultRecordInfo = null,
          timestamps = Timestamps(createdAt = Instant.now(), Instant.now(), null),
          syncStatus = SyncStatus.PENDING
      )
    }
    assertThat(records).containsNoDuplicates()

    repository.save(records).blockingAwait()
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(totalNumberOfRecords)

    // when
    teleconsultRecordSync.push()

    // then
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(0)
  }

  private fun TeleconsultRecord.syncCompleted(): TeleconsultRecord = copy(syncStatus = SyncStatus.DONE)

  private fun resetLocalData() {
    clearTeleconsultRecordData()
  }

  private fun clearTeleconsultRecordData() {
    appDatabase.teleconsultRecordDao().clear()
  }
}
