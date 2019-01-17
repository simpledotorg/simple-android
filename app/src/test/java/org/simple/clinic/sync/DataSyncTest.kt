package org.simple.clinic.sync

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import org.junit.Test
import org.simple.clinic.crash.CrashReporter

class DataSyncTest {

  @Test
  fun `when sync happens, all the model syncs should be invoked`() {
    val modelSync1 = mock<ModelSync>()
    val modelSync2 = mock<ModelSync>()
    val modelSync3 = mock<ModelSync>()
    val crashReporter = mock<CrashReporter>()

    val (sync1Completable, sync1TestConsumer) = Completable.complete().subscriptionTest()
    whenever(modelSync1.sync()).thenReturn(sync1Completable)

    val (sync2Completable, sync2TestConsumer) = Completable.complete().subscriptionTest()
    whenever(modelSync2.sync()).thenReturn(sync2Completable)

    val (sync3Completable, sync3TestConsumer) = Completable.complete().subscriptionTest()
    whenever(modelSync3.sync()).thenReturn(sync3Completable)

    val dataSync = DataSync(
        syncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        crashReporter = crashReporter)

    dataSync.sync().blockingAwait()

    sync1TestConsumer.assertInvoked()
    sync2TestConsumer.assertInvoked()
    sync3TestConsumer.assertInvoked()
  }

  @Test
  fun `when sync happens if any of the syncs throws an error, the other syncs must not be affected`() {
    val modelSync1 = mock<ModelSync>()
    val modelSync2 = mock<ModelSync>()
    val modelSync3 = mock<ModelSync>()
    val crashReporter = mock<CrashReporter>()

    val (sync1Completable, sync1TestConsumer) = Completable.complete().subscriptionTest()
    whenever(modelSync1.sync()).thenReturn(sync1Completable)

    val sync2Completable = Completable.error(RuntimeException("TEST"))
    whenever(modelSync2.sync()).thenReturn(sync2Completable)

    val (sync3Completable, sync3TestConsumer) = Completable.complete().subscriptionTest()
    whenever(modelSync3.sync()).thenReturn(sync3Completable)

    val dataSync = DataSync(
        syncs = arrayListOf(modelSync1, modelSync2, modelSync3),
        crashReporter = crashReporter)

    dataSync.sync().blockingAwait()
    sync1TestConsumer.assertInvoked()
    sync1Completable.test().assertNoErrors()
    sync3TestConsumer.assertInvoked()
    sync3Completable.test().assertNoErrors()
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
