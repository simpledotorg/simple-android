package org.simple.clinic.sync

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import org.junit.Test

class DataSyncTest {

  @Test
  fun `when syncing everything, all the model syncs should be invoked`() {
    val modelSync1 = mock<ModelSync>()
    val modelSync2 = mock<ModelSync>()
    val modelSync3 = mock<ModelSync>()

    val sync1Completable = Completable.complete()
    whenever(modelSync1.sync()).thenReturn(sync1Completable)

    val sync2Completable = Completable.complete()
    whenever(modelSync2.sync()).thenReturn(sync2Completable)

    val sync3Completable = Completable.complete()
    whenever(modelSync3.sync()).thenReturn(sync3Completable)

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        crashReporter = mock())

    dataSync.sync().blockingAwait()

    sync1Completable.test().assertComplete()
    sync2Completable.test().assertComplete()
    sync3Completable.test().assertComplete()
  }

  @Test
  fun `when syncing everything, if any of the syncs throws an error, the other syncs must not be affected`() {
    val modelSync1 = mock<ModelSync>()
    val modelSync2 = mock<ModelSync>()
    val modelSync3 = mock<ModelSync>()

    val sync1Completable = Completable.complete()
    whenever(modelSync1.sync()).thenReturn(sync1Completable)

    val sync2Completable = Completable.error(RuntimeException("TEST"))
    whenever(modelSync2.sync()).thenReturn(sync2Completable)

    val sync3Completable = Completable.complete()
    whenever(modelSync3.sync()).thenReturn(sync3Completable)

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        crashReporter = mock())

    dataSync.sync().blockingAwait()
    sync1Completable.test().assertComplete()
    sync2Completable.test().assertNotComplete()
    sync3Completable.test().assertComplete()
  }

  @Test
  fun `when syncing a particular group, only the syncs which are a part of that group must be synced`() {
    fun createModelSync(syncGroup: String, syncOperation: Completable): ModelSync {
      val modelSync = mock<ModelSync>()
      val syncConfig = SyncConfig(
          syncInterval = SyncInterval.FREQUENT,
          batchSizeEnum = BatchSize.VERY_SMALL,
          syncGroupId = syncGroup)
      whenever(modelSync.sync()).thenReturn(syncOperation)
      whenever(modelSync.syncConfig()).thenReturn(Single.just(syncConfig))

      return modelSync
    }

    val sync1Completable = Completable.complete()
    val modelSync1 = createModelSync(syncGroup = "group_1", syncOperation = sync1Completable)

    val sync2Consumer = TestDisposableConsumer()
    val sync2Completable = Completable.complete().doOnSubscribe(sync2Consumer)
    val modelSync2 = createModelSync(syncGroup = "group_2", syncOperation = sync2Completable)

    val sync3Consumer = TestDisposableConsumer()
    val sync3Completable = Completable.complete().doOnSubscribe(sync3Consumer)
    val modelSync3 = createModelSync(syncGroup = "group_3", syncOperation = sync3Completable)

    val sync4Completable = Completable.complete()
    val modelSync4 = createModelSync(syncGroup = "group_1", syncOperation = sync4Completable)

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3, modelSync4),
        crashReporter = mock())

    dataSync.syncGroup("group_1").blockingAwait()
    sync1Completable.test().assertComplete()
    sync2Consumer.assertNotInvoked()
    sync3Consumer.assertNotInvoked()
    sync4Completable.test().assertComplete()
  }

  @Test
  fun `when syncing a particular group that does not exist, none of the syncs should be executed`() {
    fun createModelSync(syncGroup: String, syncOperation: Completable): ModelSync {
      val modelSync = mock<ModelSync>()
      val syncConfig = SyncConfig(
          syncInterval = SyncInterval.FREQUENT,
          batchSizeEnum = BatchSize.VERY_SMALL,
          syncGroupId = syncGroup)
      whenever(modelSync.sync()).thenReturn(syncOperation)
      whenever(modelSync.syncConfig()).thenReturn(Single.just(syncConfig))

      return modelSync
    }

    val sync1Consumer = TestDisposableConsumer()
    val sync1Completable = Completable.complete().doOnSubscribe(sync1Consumer)
    val modelSync1 = createModelSync(syncGroup = "group_1", syncOperation = sync1Completable)

    val sync2Consumer = TestDisposableConsumer()
    val sync2Completable = Completable.complete().doOnSubscribe(sync2Consumer)
    val modelSync2 = createModelSync(syncGroup = "group_2", syncOperation = sync2Completable)

    val sync3Consumer = TestDisposableConsumer()
    val sync3Completable = Completable.complete().doOnSubscribe(sync3Consumer)
    val modelSync3 = createModelSync(syncGroup = "group_3", syncOperation = sync3Completable)

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        crashReporter = mock())

    dataSync.syncGroup("missing_group").blockingAwait()
    sync1Consumer.assertNotInvoked()
    sync2Consumer.assertNotInvoked()
    sync3Consumer.assertNotInvoked()
  }

  @Test
  fun `when syncing a particular group, errors in syncing any should not affect another syncs`() {
    fun createModelSync(syncGroup: String, syncOperation: Completable): ModelSync {
      val modelSync = mock<ModelSync>()
      val syncConfig = SyncConfig(
          syncInterval = SyncInterval.FREQUENT,
          batchSizeEnum = BatchSize.VERY_SMALL,
          syncGroupId = syncGroup)
      whenever(modelSync.sync()).thenReturn(syncOperation)
      whenever(modelSync.syncConfig()).thenReturn(Single.just(syncConfig))

      return modelSync
    }

    val sync1Completable = Completable.complete()
    val modelSync1 = createModelSync(syncGroup = "group_1", syncOperation = sync1Completable)

    val sync2Completable = Completable.error(RuntimeException("TEST"))
    val modelSync2 = createModelSync(syncGroup = "group_1", syncOperation = sync2Completable)

    val modelSync3 = createModelSync(syncGroup = "group_2", syncOperation = Completable.complete())

    val sync4Completable = Completable.complete()
    val modelSync4 = createModelSync(syncGroup = "group_1", syncOperation = sync4Completable)

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3, modelSync4),
        crashReporter = mock())

    dataSync.syncGroup("group_1").blockingAwait()
    sync1Completable.test().assertComplete()
    sync2Completable.test().assertNotComplete()
    sync4Completable.test().assertComplete()
  }

  inner class TestDisposableConsumer : Consumer<Disposable> {

    private var invoked: Boolean = false

    override fun accept(t: Disposable?) {
      invoked = true
    }

    fun assertNotInvoked() {
      if (invoked) {
        throw AssertionError("INVOKED!")
      }
    }
  }
}
