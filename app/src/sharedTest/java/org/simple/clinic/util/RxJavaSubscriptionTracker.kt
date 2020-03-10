package org.simple.clinic.util

import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.CompletableSource
import io.reactivex.plugins.RxJavaPlugins

class RxJavaSubscriptionTracker {

  private var assembledCompletables = mutableListOf<CompletableOnAssembly>()

  fun startTracking() {
    RxJavaPlugins.setOnCompletableAssembly { source ->
      val assembly = CompletableOnAssembly(source)

      assembledCompletables.add(assembly)

      assembly
    }
  }

  fun stopTracking() {
    RxJavaPlugins.setOnCompletableAssembly(null)
  }

  fun assertAllCompletablesSubscribed(
      expectUnsubscribed: ExpectUnsubscribed? = null
  ) {
    val assembledCount = assembledCompletables.size
    val subscribedCount = assembledCompletables.count { it.hasBeenSubscribedTo }
    val expectedUnsubscribedCount = expectUnsubscribed?.completables ?: 0
    val totalUnsubscribedCount = assembledCount - subscribedCount

    if (totalUnsubscribedCount != expectedUnsubscribedCount) {
      val unsubscribedCompletables = assembledCompletables
          .filterNot { it.hasBeenSubscribedTo }
          .mapIndexed { index, assembly -> "#%02d - %s".format(index, assembly.createdAt) }
          .joinToString("\n")

      val message = """
        |
        |Found unsubscribed Completables!
        |--------------------------------
        |Assembled: $assembledCount
        |Subscribed: $subscribedCount
        |Expected to be not subscribed to: $expectedUnsubscribedCount
        |--------------------------------
        |$unsubscribedCompletables
        |--------------------------------
      """.trimMargin()
      throw AssertionError(message)
    }
  }

  private class CompletableOnAssembly(
      private val source: CompletableSource
  ) : Completable() {

    private val stackTrace = Thread
        .currentThread()
        .stackTrace

    val createdAt: String = stackTrace[7].toString()

    var hasBeenSubscribedTo: Boolean = false
      private set

    override fun subscribeActual(observer: CompletableObserver) {
      hasBeenSubscribedTo = true
      source.subscribe(observer)
    }
  }
}
