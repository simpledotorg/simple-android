package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
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
import org.simple.clinic.util.TestUtcClock
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.util.UUID
import javax.inject.Inject

class TeleconsultRecordSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var teleconsultRecordApi: TeleconsultRecordApi

  @Inject
  lateinit var repository: TeleconsultRecordRepository

  @Inject
  lateinit var testUtcClock: TestUtcClock

  private lateinit var teleconsultRecordSync: TeleconsultRecordSync

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  private val batchSize = 0
  private val config = SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = batchSize,
      syncGroup = SyncGroup.FREQUENT
  )

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    testUtcClock.setDate(LocalDate.of(2020, Month.SEPTEMBER, 28))
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
      val requestInfo = TestData.teleconsultRequestInfo(
          requesterId = UUID.fromString("73e28c27-4e79-4409-b2a6-7ac562b0ecd7"),
          facilityId = UUID.fromString("5fc76656-7e4f-4c85-b981-076bd18c9836"),
          requestedAt = Instant.now(testUtcClock),
          requestCompleted = TeleconsultStatus.Yes
      )

      val recordInfo = TestData.teleconsultRecordInfo(
          recordedAt = Instant.now(testUtcClock),
          teleconsultationType = TeleconsultationType.Audio,
          patientTookMedicines = Answer.Yes,
          patientConsented = Answer.Yes,
          medicalOfficerNumber = "+911111111111"
      )

      TestData.teleconsultRecord(
          id = teleconsultRecordId,
          patientId = UUID.fromString("92974564-9194-4a04-b4bd-5ac1fba82e31"),
          medicalOfficerId = UUID.fromString("39f4307b-0aef-49d4-9578-371524cd0212"),
          teleconsultRequestInfo = requestInfo,
          teleconsultRecordInfo = recordInfo,
          timestamps = Timestamps.create(testUtcClock),
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
