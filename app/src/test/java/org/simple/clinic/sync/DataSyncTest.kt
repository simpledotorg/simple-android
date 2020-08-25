package org.simple.clinic.sync

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class DataSyncTest {

  @get:Rule
  val rule = RxErrorsRule()

  private val schedulersProvider = TrampolineSchedulersProvider()

  @Test
  fun `when syncing everything, all the model syncs should be invoked`() {
    val modelSync1 = mock<ModelSync>()
    val modelSync2 = mock<ModelSync>()
    val modelSync3 = mock<ModelSync>()

    val (sync1Completable, sync1Consumer) = Completable.complete().subscriptionTest()
    whenever(modelSync1.syncConfig()).thenReturn(createSyncConfig(SyncGroup.DAILY))
    whenever(modelSync1.sync()).thenReturn(sync1Completable)

    val (sync2Completable, sync2Consumer) = Completable.complete().subscriptionTest()
    whenever(modelSync2.syncConfig()).thenReturn(createSyncConfig(SyncGroup.DAILY))
    whenever(modelSync2.sync()).thenReturn(sync2Completable)

    val (sync3Completable, sync3Consumer) = Completable.complete().subscriptionTest()
    whenever(modelSync3.syncConfig()).thenReturn(createSyncConfig(SyncGroup.FREQUENT))
    whenever(modelSync3.sync()).thenReturn(sync3Completable)

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        crashReporter = mock(),
        schedulersProvider = schedulersProvider
    )

    dataSync.syncTheWorld().blockingAwait()

    sync1Consumer.assertInvoked()
    sync2Consumer.assertInvoked()
    sync3Consumer.assertInvoked()
  }

  @Test
  fun `when syncing everything, if any of the syncs throws an error, the other syncs must not be affected`() {
    val modelSync1 = mock<ModelSync>()
    val (sync1Completable, sync1Consumer) = Completable.complete().subscriptionTest()
    whenever(modelSync1.syncConfig()).thenReturn(createSyncConfig(SyncGroup.DAILY))
    whenever(modelSync1.sync()).thenReturn(sync1Completable)
    whenever(modelSync1.name).thenReturn("sync1")

    val modelSync2 = mock<ModelSync>()
    val sync2Completable = Completable.error(RuntimeException("TEST"))
    whenever(modelSync2.syncConfig()).thenReturn(createSyncConfig(SyncGroup.DAILY))
    whenever(modelSync2.sync()).thenReturn(sync2Completable)
    whenever(modelSync2.name).thenReturn("sync2")

    val modelSync3 = mock<ModelSync>()
    val (sync3Completable, sync3Consumer) = Completable.complete().subscriptionTest()
    whenever(modelSync3.syncConfig()).thenReturn(createSyncConfig(SyncGroup.FREQUENT))
    whenever(modelSync3.sync()).thenReturn(sync3Completable)
    whenever(modelSync3.name).thenReturn("sync3")

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        crashReporter = mock(),
        schedulersProvider = schedulersProvider
    )

    dataSync.syncTheWorld().blockingAwait()

    sync1Consumer.assertInvoked()
    sync1Completable.test().assertNoErrors()
    sync3Consumer.assertInvoked()
  }

  fun createSyncConfig(syncGroup: SyncGroup) =
      Single.just(SyncConfig(
          syncInterval = SyncInterval.FREQUENT,
          batchSize = 10,
          syncGroup = syncGroup))


  @Test
  fun `when syncing a particular group, only the syncs which are a part of that group must be synced`() {
    fun createModelSync(syncGroup: SyncGroup, syncOperation: Completable): ModelSync {
      val modelSync = mock<ModelSync>()
      val syncConfig = SyncConfig(
          syncInterval = SyncInterval.FREQUENT,
          batchSize = 10,
          syncGroup = syncGroup)
      whenever(modelSync.sync()).thenReturn(syncOperation)
      whenever(modelSync.syncConfig()).thenReturn(Single.just(syncConfig))

      return modelSync
    }

    val (sync1Completable, sync1Consumer) = Completable.complete().subscriptionTest()
    val modelSync1 = createModelSync(syncGroup = SyncGroup.FREQUENT, syncOperation = sync1Completable)

    val (sync2Completable, sync2Consumer) = Completable.complete().subscriptionTest()
    val modelSync2 = createModelSync(syncGroup = SyncGroup.DAILY, syncOperation = sync2Completable)

    val (sync3Completable, sync3Consumer) = Completable.complete().subscriptionTest()
    val modelSync3 = createModelSync(syncGroup = SyncGroup.DAILY, syncOperation = sync3Completable)

    val (sync4Completable, sync4Consumer) = Completable.complete().subscriptionTest()
    val modelSync4 = createModelSync(syncGroup = SyncGroup.FREQUENT, syncOperation = sync4Completable)

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3, modelSync4),
        crashReporter = mock(),
        schedulersProvider = schedulersProvider
    )

    dataSync.sync(SyncGroup.FREQUENT).blockingAwait()
    sync1Consumer.assertInvoked()
    sync2Consumer.assertNotInvoked()
    sync3Consumer.assertNotInvoked()
    sync4Consumer.assertInvoked()
  }

  @Test
  fun `when syncing a particular group, errors in syncing any should not affect another syncs`() {
    fun createModelSync(syncGroup: SyncGroup, syncOperation: Completable): ModelSync {
      val modelSync = mock<ModelSync>()
      val syncConfig = SyncConfig(
          syncInterval = SyncInterval.FREQUENT,
          batchSize = 10,
          syncGroup = syncGroup)
      whenever(modelSync.sync()).thenReturn(syncOperation)
      whenever(modelSync.syncConfig()).thenReturn(Single.just(syncConfig))

      return modelSync
    }

    val (sync1Completable, sync1Consumer) = Completable.complete().subscriptionTest()
    val modelSync1 = createModelSync(syncGroup = SyncGroup.FREQUENT, syncOperation = sync1Completable)

    val sync2Completable = Completable.error(RuntimeException("TEST"))
    val modelSync2 = createModelSync(syncGroup = SyncGroup.DAILY, syncOperation = sync2Completable)

    val modelSync3 = createModelSync(syncGroup = SyncGroup.DAILY, syncOperation = Completable.complete())

    val (sync4Completable, sync4Consumer) = Completable.complete().subscriptionTest()
    val modelSync4 = createModelSync(syncGroup = SyncGroup.FREQUENT, syncOperation = sync4Completable)

    val dataSync = DataSync(
        modelSyncs = arrayListOf(modelSync1, modelSync2, modelSync3, modelSync4),
        crashReporter = mock(),
        schedulersProvider = schedulersProvider
    )

    dataSync.sync(SyncGroup.FREQUENT).blockingAwait()
    sync1Consumer.assertInvoked()
    sync4Consumer.assertInvoked()
  }

  inner class TestDisposableConsumer : Consumer<Disposable> {

    private var invoked: Boolean = false

    override fun accept(t: Disposable?) {
      invoked = true
    }

    fun assertInvoked() {
      if (!invoked) {
        throw AssertionError("NOT INVOKED!")
      }
    }

    fun assertNotInvoked() {
      if (invoked) {
        throw AssertionError("INVOKED!")
      }
    }
  }

  private fun Completable.subscriptionTest(): Pair<Completable, DataSyncTest.TestDisposableConsumer> {
    val consumer = TestDisposableConsumer()
    return this.doOnSubscribe(consumer) to consumer
  }
}
