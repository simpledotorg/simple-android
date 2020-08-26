package org.simple.clinic.sync

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.ResolvedError
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

    whenever(modelSync1.syncConfig()).thenReturn(createSyncConfig(SyncGroup.DAILY))
    whenever(modelSync1.sync()).thenReturn(Completable.complete())

    whenever(modelSync2.syncConfig()).thenReturn(createSyncConfig(SyncGroup.DAILY))
    whenever(modelSync2.sync()).thenReturn(Completable.complete())

    whenever(modelSync3.syncConfig()).thenReturn(createSyncConfig(SyncGroup.FREQUENT))
    whenever(modelSync3.sync()).thenReturn(Completable.complete())

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
    val modelSync1 = mock<ModelSync>()
    whenever(modelSync1.syncConfig()).thenReturn(createSyncConfig(SyncGroup.DAILY))
    whenever(modelSync1.sync()).thenReturn(Completable.complete())
    whenever(modelSync1.name).thenReturn("sync1")

    val modelSync2 = mock<ModelSync>()
    whenever(modelSync2.syncConfig()).thenReturn(createSyncConfig(SyncGroup.DAILY))
    val runtimeException = RuntimeException("TEST")
    whenever(modelSync2.sync()).thenReturn(Completable.error(runtimeException))
    whenever(modelSync2.name).thenReturn("sync2")

    val modelSync3 = mock<ModelSync>()
    whenever(modelSync3.syncConfig()).thenReturn(createSyncConfig(SyncGroup.FREQUENT))
    whenever(modelSync3.sync()).thenReturn(Completable.complete())
    whenever(modelSync3.name).thenReturn("sync3")

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

  private fun createSyncConfig(syncGroup: SyncGroup) = SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = 10,
      syncGroup = syncGroup
  )

  @Test
  fun `when syncing a particular group, only the syncs which are a part of that group must be synced`() {
    fun createModelSync(syncGroup: SyncGroup, syncOperation: Completable): ModelSync {
      val modelSync = mock<ModelSync>()
      val syncConfig = SyncConfig(
          syncInterval = SyncInterval.FREQUENT,
          batchSize = 10,
          syncGroup = syncGroup)
      whenever(modelSync.sync()).thenReturn(syncOperation)
      whenever(modelSync.syncConfig()).thenReturn(syncConfig)

      return modelSync
    }

    val modelSync1 = createModelSync(syncGroup = SyncGroup.FREQUENT, syncOperation = Completable.complete())
    val modelSync2 = createModelSync(syncGroup = SyncGroup.DAILY, syncOperation = Completable.error(RuntimeException()))
    val modelSync3 = createModelSync(syncGroup = SyncGroup.DAILY, syncOperation = Completable.error(RuntimeException()))
    val modelSync4 = createModelSync(syncGroup = SyncGroup.FREQUENT, syncOperation = Completable.complete())

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
    fun createModelSync(syncGroup: SyncGroup, syncOperation: Completable): ModelSync {
      val modelSync = mock<ModelSync>()
      val syncConfig = SyncConfig(
          syncInterval = SyncInterval.FREQUENT,
          batchSize = 10,
          syncGroup = syncGroup)
      whenever(modelSync.sync()).thenReturn(syncOperation)
      whenever(modelSync.syncConfig()).thenReturn(syncConfig)

      return modelSync
    }

    val modelSync1 = createModelSync(syncGroup = SyncGroup.FREQUENT, syncOperation = Completable.complete())
    whenever(modelSync1.name).thenReturn("sync1")

    val runtimeException = RuntimeException("TEST")
    val modelSync2 = createModelSync(syncGroup = SyncGroup.FREQUENT, syncOperation = Completable.error(runtimeException))
    whenever(modelSync2.name).thenReturn("sync2")

    val modelSync3 = createModelSync(syncGroup = SyncGroup.DAILY, syncOperation = Completable.complete())
    whenever(modelSync3.name).thenReturn("sync3")

    val modelSync4 = createModelSync(syncGroup = SyncGroup.FREQUENT, syncOperation = Completable.complete())
    whenever(modelSync4.name).thenReturn("sync4")

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
        .sync(SyncGroup.FREQUENT)
        .test()
        .assertNoErrors()
        .assertComplete()
        .dispose()

    syncErrors
        .assertValue(ResolvedError.Unexpected(runtimeException))
        .dispose()
  }
}
