package org.simple.clinic.sync

import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Completable
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.ResolvedError
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class DataSyncTest {

  @get:Rule
  val rule = RxErrorsRule()

  private val schedulersProvider = TestSchedulersProvider.trampoline()

  private val frequentSyncConfig = SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = 10,
      syncGroup = SyncGroup.FREQUENT
  )

  private val dailySyncConfig = SyncConfig(
      syncInterval = SyncInterval.DAILY,
      batchSize = 10,
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
        crashReporter = mock(),
        schedulersProvider = schedulersProvider
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()

    dataSync
        .syncTheWorld()
        .test()
        .assertNoErrors()
        .assertComplete()
        .dispose()

    syncErrors
        .assertNoErrors()
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
        crashReporter = mock(),
        schedulersProvider = schedulersProvider
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync
        .syncTheWorld()
        .test()
        .assertNoErrors()
        .assertComplete()
        .dispose()

    syncErrors
        .assertValue(ResolvedError.Unexpected(runtimeException))
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
        crashReporter = mock(),
        schedulersProvider = schedulersProvider
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()

    dataSync
        .sync(SyncGroup.FREQUENT)
        .test()
        .assertNoErrors()
        .assertComplete()
        .dispose()

    syncErrors
        .assertNoErrors()
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
        crashReporter = mock(),
        schedulersProvider = schedulersProvider
    )

    val syncErrors = dataSync
        .streamSyncErrors()
        .test()
        .assertNoErrors()

    dataSync
        .sync(SyncGroup.DAILY)
        .test()
        .assertNoErrors()
        .assertComplete()
        .dispose()

    syncErrors
        .assertValue(ResolvedError.Unexpected(runtimeException))
        .dispose()
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
      return Completable.mergeArrayDelayError(push(), pull())
    }

    override fun push(): Completable = if (pushError == null) Completable.complete() else Completable.error(pushError)

    override fun pull(): Completable = if (pullError == null) Completable.complete() else Completable.error(pullError)

    override fun syncConfig(): SyncConfig = config
  }
}
