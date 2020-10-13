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
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.sync.BloodSugarSync
import org.simple.clinic.bloodsugar.sync.BloodSugarSyncApi
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import org.simple.clinic.util.Rules
import org.simple.clinic.util.unsafeLazy
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class BloodSugarSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var repository: BloodSugarRepository

  @Inject
  @Named("last_blood_sugar_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: BloodSugarSyncApi

  @Inject
  lateinit var userSession: UserSession

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  private lateinit var sync: BloodSugarSync

  private val batchSize = 3
  private val config = SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = batchSize,
      syncTag = SyncTag.FREQUENT
  )

  private val currentFacilityUuid: UUID by unsafeLazy { userSession.loggedInUserImmediate()!!.currentFacilityUuid }

  private val userUuid: UUID by unsafeLazy { userSession.loggedInUserImmediate()!!.uuid }

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    resetLocalData()

    sync = BloodSugarSync(
        syncCoordinator = SyncCoordinator(),
        api = syncApi,
        repository = repository,
        lastPullToken = lastPullToken,
        config = config
    )
  }

  @After
  fun tearDown() {
    resetLocalData()
  }

  private fun resetLocalData() {
    clearBloodSugarData()
    lastPullToken.delete()
  }

  private fun clearBloodSugarData() {
    appDatabase.bloodSugarDao().clear()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // given
    val totalNumberOfRecords = batchSize * 2 + 1
    val records = (1..totalNumberOfRecords).map {
      TestData.bloodSugarMeasurement(
          syncStatus = SyncStatus.PENDING,
          facilityUuid = currentFacilityUuid,
          userUuid = userUuid
      )
    }
    assertThat(records).containsNoDuplicates()

    repository.save(records).blockingAwait()
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(totalNumberOfRecords)

    // when
    sync.push()
    clearBloodSugarData()
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
      TestData.bloodSugarMeasurement(
          syncStatus = SyncStatus.PENDING,
          facilityUuid = currentFacilityUuid,
          userUuid = userUuid
      )
    }
    assertThat(records).containsNoDuplicates()

    repository.save(records).blockingAwait()
    sync.push()
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(0)

    val modifiedRecord = records[1].withReading("42")
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

  private fun BloodSugarMeasurement.syncCompleted(): BloodSugarMeasurement = copy(syncStatus = SyncStatus.DONE)

  private fun BloodSugarMeasurement.withReading(value: String): BloodSugarMeasurement {
    return copy(
        reading = reading.copy(value = value),
        syncStatus = SyncStatus.PENDING,
        timestamps = timestamps.copy(updatedAt = timestamps.updatedAt.plusMillis(1))
    )
  }
}
