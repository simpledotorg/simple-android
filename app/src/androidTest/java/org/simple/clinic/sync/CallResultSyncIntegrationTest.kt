package org.simple.clinic.sync

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.sharedTestCode.TestData
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.overdue.callresult.CallResultRepository
import org.simple.clinic.overdue.callresult.CallResultSync
import org.simple.clinic.overdue.callresult.CallResultSyncApi
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.sharedTestCode.util.Rules
import org.simple.sharedTestCode.util.TestUtcClock
import org.simple.clinic.util.UtcClock
import org.simple.sharedTestCode.util.randomRemoveReason
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class CallResultSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var syncApi: CallResultSyncApi

  @Inject
  lateinit var repository: CallResultRepository

  @Inject
  lateinit var testUtcClock: TestUtcClock

  @Inject
  lateinit var syncInterval: SyncInterval

  @Inject
  lateinit var userSession: UserSession

  private val currentUser: User by lazy { userSession.loggedInUserImmediate()!! }

  private lateinit var recordSync: CallResultSync

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  private val batchSize = 3
  private lateinit var config: SyncConfig

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    testUtcClock.setDate(LocalDate.parse("2021-08-30"))
    resetLocalData()

    config = SyncConfig(
        syncInterval = syncInterval,
        pullBatchSize = batchSize,
        pushBatchSize = batchSize,
        name = ""
    )

    recordSync = CallResultSync(
        syncCoordinator = SyncCoordinator(),
        api = syncApi,
        repository = repository,
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
    val appointment = TestData.appointment(uuid = UUID.fromString("3e3d0977-e6f2-460f-8146-a7eae42a8cce"))
    val records = (1..totalNumberOfRecords).map {
      randomCallResult(currentUser, appointment, testUtcClock, SyncStatus.PENDING)
    }
    assertThat(records).containsNoDuplicates()

    repository.save(records)
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(totalNumberOfRecords)

    // when
    recordSync.push()

    // then
    assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(0)
  }

  private fun randomCallResult(
      user: User,
      appointment: Appointment,
      clock: UtcClock,
      syncStatus: SyncStatus
  ): CallResult {
    val callResultId = UUID.randomUUID()
    val diceRoll = Random.nextFloat()

    return when {
      diceRoll < 0.33 -> CallResult.agreedToVisit(callResultId, appointment, user, clock, syncStatus)
      diceRoll < 0.66 -> CallResult.remindToCallLater(callResultId, appointment, user, clock, syncStatus)
      else -> CallResult.removed(callResultId, randomRemoveReason(), appointment, user, clock, syncStatus)
    }
  }

  private fun resetLocalData() {
    appDatabase.callResultDao().clear()
  }
}
