package org.simple.clinic.sync

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.ErrorResolver
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.io.IOException
import java.time.Duration
import java.util.UUID

class DataSyncTest {

  @get:Rule
  val rule = RxErrorsRule()

  private val schedulersProvider = TestSchedulersProvider.trampoline()
  private val userSession = mock<UserSession>()

  private val frequentSyncConfig = SyncConfig(
      syncInterval = SyncInterval(
          frequency = Duration.ofMinutes(16),
          backOffDelay = Duration.ofMinutes(5)
      ),
      pullBatchSize = 10,
      pushBatchSize = 10,
      syncGroup = SyncGroup.FREQUENT
  )

  private val dailySyncConfig = SyncConfig(
      syncInterval = SyncInterval(
          frequency = Duration.ofMinutes(16),
          backOffDelay = Duration.ofMinutes(5)
      ),
      pullBatchSize = 10,
      pushBatchSize = 10,
      syncGroup = SyncGroup.DAILY
  )

  @Test
  fun `when syncing everything, all the model syncs should be invoked`() {
    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = dailySyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        config = dailySyncConfig
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()

    dataSync.syncTheWorld()

    syncErrors
        .assertNoErrors()
        .assertNoValues()
        .dispose()
  }

  @Test
  fun `when syncing everything, if any of the syncs throws an error, the other syncs must not be affected`() {
    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = dailySyncConfig
    )

    val runtimeException = RuntimeException("TEST")
    val modelSync2 = FakeModelSync(
        _name = "sync2",
        config = dailySyncConfig,
        pullError = runtimeException
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync.syncTheWorld()

    syncErrors
        .assertValue(ResolvedError.Unexpected(runtimeException))
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `when syncing a particular group, only the syncs which are a part of that group must be synced`() {
    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = frequentSyncConfig
    )
    val modelSync2 = FakeModelSync(
        _name = "sync2",
        config = dailySyncConfig,
        pushError = RuntimeException()
    )
    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = dailySyncConfig,
        pullError = RuntimeException()
    )
    val modelSync4 = FakeModelSync(
        _name = "sync4",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3, modelSync4),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()

    dataSync.sync(SyncGroup.FREQUENT)

    syncErrors
        .assertNoErrors()
        .assertNoValues()
        .dispose()
  }

  @Test
  fun `when syncing a particular group, errors in syncing any should not affect another syncs`() {
    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = frequentSyncConfig
    )

    val runtimeException = RuntimeException("TEST")
    val modelSync2 = FakeModelSync(
        _name = "sync2",
        config = dailySyncConfig,
        pushError = runtimeException
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = dailySyncConfig,
    )

    val modelSync4 = FakeModelSync(
        _name = "sync4",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3, modelSync4),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync.sync(SyncGroup.DAILY)

    syncErrors
        .assertValue(ResolvedError.Unexpected(runtimeException))
        .dispose()
  }

  @Test
  fun `when syncing everything, syncs which require a sync approved user should be skipped if there is no user present`() {
    whenever(userSession.loggedInUserImmediate()).thenReturn(null)

    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = dailySyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        _requiresSyncApprovedUser = true,
        config = dailySyncConfig,
        pullError = RuntimeException("TEST"),
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync.syncTheWorld()

    syncErrors
        .assertNoValues()
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `when syncing everything, syncs which require a sync approved user should be skipped if there is a user present and they are waiting for login OTP`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("54cb095a-71f3-412d-8587-dc450a7a47b9"),
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED,
        status = UserStatus.ApprovedForSyncing
    )
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)

    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = dailySyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        _requiresSyncApprovedUser = true,
        config = dailySyncConfig,
        pullError = RuntimeException("TEST"),
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync.syncTheWorld()

    syncErrors
        .assertNoValues()
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `when syncing everything, syncs which require a sync approved user should be skipped if there is a user present and they are resetting the login pin`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("54cb095a-71f3-412d-8587-dc450a7a47b9"),
        loggedInStatus = User.LoggedInStatus.RESETTING_PIN,
        status = UserStatus.ApprovedForSyncing
    )
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)

    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = dailySyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        _requiresSyncApprovedUser = true,
        config = dailySyncConfig,
        pullError = RuntimeException("TEST"),
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync.syncTheWorld()

    syncErrors
        .assertNoValues()
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `when syncing everything, syncs which require a sync approved user should be skipped if there is a user present and they requested a login PIN reset`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("54cb095a-71f3-412d-8587-dc450a7a47b9"),
        loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED,
        status = UserStatus.WaitingForApproval
    )
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)

    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = dailySyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        _requiresSyncApprovedUser = true,
        config = dailySyncConfig,
        pullError = RuntimeException("TEST"),
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync.syncTheWorld()

    syncErrors
        .assertNoValues()
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `when syncing everything, syncs which require a sync approved user should be skipped if there is a user present and they have logged in on another device`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("54cb095a-71f3-412d-8587-dc450a7a47b9"),
        loggedInStatus = User.LoggedInStatus.UNAUTHORIZED,
        status = UserStatus.ApprovedForSyncing
    )
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)

    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = dailySyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        _requiresSyncApprovedUser = true,
        config = dailySyncConfig,
        pullError = RuntimeException("TEST"),
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync.syncTheWorld()

    syncErrors
        .assertNoValues()
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `when syncing everything, syncs which require a sync approved user should be executed if there is a user present and they are logged in and approved for syncing`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("54cb095a-71f3-412d-8587-dc450a7a47b9"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        status = UserStatus.ApprovedForSyncing
    )
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)

    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = dailySyncConfig
    )

    val exception = RuntimeException("TEST")
    val modelSync2 = FakeModelSync(
        _name = "sync2",
        _requiresSyncApprovedUser = true,
        config = dailySyncConfig,
        pullError = exception,
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync.syncTheWorld()

    syncErrors
        .assertValue(ResolvedError.Unexpected(exception))
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `when syncing everything, syncs which require a sync approved user should be skipped if there is a user present and they are logged in and waiting for approval`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("54cb095a-71f3-412d-8587-dc450a7a47b9"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        status = UserStatus.WaitingForApproval
    )
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)

    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = dailySyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        _requiresSyncApprovedUser = true,
        config = dailySyncConfig,
        pullError = RuntimeException("TEST"),
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync.syncTheWorld()

    syncErrors
        .assertNoValues()
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `when syncing everything, syncs which require a sync approved user should be skipped if there is a user present and they are logged in and disapproved for syncing`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("54cb095a-71f3-412d-8587-dc450a7a47b9"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        status = UserStatus.DisapprovedForSyncing
    )
    whenever(userSession.loggedInUserImmediate()).thenReturn(user)

    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = dailySyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        _requiresSyncApprovedUser = true,
        config = dailySyncConfig,
        pullError = RuntimeException("TEST"),
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync.syncTheWorld()

    syncErrors
        .assertNoValues()
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `when syncing a group, if multiple syncs throws an error, the overall sync result must be a failure`() {
    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = frequentSyncConfig
    )

    val runtimeException = RuntimeException("TEST")
    val ioException = IOException("TEST")

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        config = frequentSyncConfig,
        pullError = runtimeException
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig,
        pushError = ioException
    )

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = mock()
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    val syncResults = dataSync
        .streamSyncResults()
        .test()
        .assertNoErrors()

    dataSync.sync(SyncGroup.FREQUENT)

    syncErrors
        .assertValue(ErrorResolver.resolve(ioException)) // IOException because it is a push error and pushes are executed first
        .assertNoErrors()
        .dispose()

    syncResults
        .assertValues(
            DataSync.SyncGroupResult(SyncGroup.FREQUENT, SyncProgress.SYNCING),
            DataSync.SyncGroupResult(SyncGroup.FREQUENT, SyncProgress.FAILURE)
        )
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `when syncing a group, the unused data must never be purged`() {
    // given
    whenever(userSession.isUserPresentLocally()).thenReturn(true)

    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = frequentSyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        config = frequentSyncConfig
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = frequentSyncConfig
    )

    val purgeOnSync = mock<PurgeOnSync>()
    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = purgeOnSync
    )

    // when
    dataSync.sync(SyncGroup.FREQUENT)

    // then
    verifyZeroInteractions(purgeOnSync)
  }

  @Test
  fun `when syncing everything and there is a user present locally, the unused data must be purged if all syncs completed successfully`() {
    // given
    whenever(userSession.isUserPresentLocally()).thenReturn(true)
    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = frequentSyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        config = frequentSyncConfig
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = dailySyncConfig
    )

    val purgeOnSync = mock<PurgeOnSync>()
    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = purgeOnSync
    )

    // when
    dataSync.syncTheWorld()

    // then
    verify(purgeOnSync).purgeUnusedData()
    verifyNoMoreInteractions(purgeOnSync)
  }

  @Test
  fun `when syncing everything and there is no user present locally, the unused data must not be purged even if all syncs completed successfully`() {
    // given
    whenever(userSession.isUserPresentLocally()).thenReturn(false)
    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = frequentSyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        config = frequentSyncConfig
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = dailySyncConfig
    )

    val purgeOnSync = mock<PurgeOnSync>()
    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = purgeOnSync
    )

    // when
    dataSync.syncTheWorld()

    // then
    verify(purgeOnSync, never()).purgeUnusedData()
    verifyNoMoreInteractions(purgeOnSync)
  }

  @Test
  fun `if a pull operation fails when syncing everything, the unused data must not be purged`() {
    // given
    whenever(userSession.isUserPresentLocally()).thenReturn(true)
    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = frequentSyncConfig,
        pullError = RuntimeException()
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        config = frequentSyncConfig
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = dailySyncConfig
    )

    val purgeOnSync = mock<PurgeOnSync>()
    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = purgeOnSync
    )

    // when
    dataSync.syncTheWorld()

    // then
    verify(purgeOnSync, never()).purgeUnusedData()
    verifyNoMoreInteractions(purgeOnSync)
  }

  @Test
  fun `if a push operation fails when syncing everything, the unused data must not be purged`() {
    // given
    whenever(userSession.isUserPresentLocally()).thenReturn(true)
    val modelSync1 = FakeModelSync(
        _name = "sync1",
        config = frequentSyncConfig
    )

    val modelSync2 = FakeModelSync(
        _name = "sync2",
        config = frequentSyncConfig
    )

    val modelSync3 = FakeModelSync(
        _name = "sync3",
        config = dailySyncConfig,
        pushError = RuntimeException()
    )

    val purgeOnSync = mock<PurgeOnSync>()
    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        userSession = userSession,
        schedulersProvider = schedulersProvider,
        syncScheduler = schedulersProvider.io(),
        purgeOnSync = purgeOnSync
    )

    // when
    dataSync.syncTheWorld()

    // then
    verify(purgeOnSync, never()).purgeUnusedData()
    verifyNoMoreInteractions(purgeOnSync)
  }

  private class FakeModelSync(
      _name: String,
      _requiresSyncApprovedUser: Boolean = false,
      private val config: SyncConfig,
      private val pushError: Throwable? = null,
      private val pullError: Throwable? = null,
  ) : ModelSync {

    override val name: String = _name

    override val requiresSyncApprovedUser: Boolean = _requiresSyncApprovedUser

    override fun sync(): Completable {
      return Completable
          .mergeArrayDelayError(
              Completable.fromAction { push() },
              Completable.fromAction { pull() }
          )
    }

    override fun push() {
      if (pushError != null) throw pushError
    }

    override fun pull() {
      if (pullError != null) throw pullError
    }

    override fun syncConfig(): SyncConfig = config
  }
}
