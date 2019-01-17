package org.simple.clinic.sync

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import org.junit.Test
import org.simple.clinic.crash.CrashReporter

class DataSyncTest {

  @Test
  fun `when sync happens, all the model syncs should be invoked`() {
    val modelSync1 = mock<ModelSync>()
    val modelSync2 = mock<ModelSync>()
    val modelSync3 = mock<ModelSync>()
    val crashReporter = mock<CrashReporter>()

    val sync1Completable = Completable.complete()
    whenever(modelSync1.sync()).thenReturn(sync1Completable)

    val sync2Completable = Completable.complete()
    whenever(modelSync2.sync()).thenReturn(sync2Completable)

    val sync3Completable = Completable.complete()
    whenever(modelSync3.sync()).thenReturn(sync3Completable)

    val dataSync = DataSync(
        syncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        crashReporter = crashReporter)

    dataSync.sync().blockingAwait()

    sync1Completable.test().assertComplete()
    sync2Completable.test().assertComplete()
    sync3Completable.test().assertComplete()
  }

  @Test
  fun `when sync happens if any of the syncs throws an error, the other syncs must not be affected`() {
    val modelSync1 = mock<ModelSync>()
    val modelSync2 = mock<ModelSync>()
    val modelSync3 = mock<ModelSync>()
    val crashReporter = mock<CrashReporter>()

    val sync1Completable = Completable.complete()
    whenever(modelSync1.sync()).thenReturn(sync1Completable)

    val sync2Completable = Completable.error(RuntimeException("TEST"))
    whenever(modelSync2.sync()).thenReturn(sync2Completable)

    val sync3Completable = Completable.complete()
    whenever(modelSync3.sync()).thenReturn(sync3Completable)

    val dataSync = DataSync(
        syncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        crashReporter = crashReporter)

    dataSync.sync().blockingAwait()
    sync1Completable.test().assertComplete()
    sync3Completable.test().assertComplete()
  }
}
