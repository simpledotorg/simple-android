package org.simple.clinic.platform.crash

import timber.log.Timber

object CrashReporter {

  private var sinks = emptyList<Sink>()

  fun dropBreadcrumb(breadcrumb: Breadcrumb) {
    sinks.forEach { sink ->
      runSafely { sink.dropBreadcrumb(breadcrumb) }
    }
  }

  fun report(e: Throwable) {
    sinks.forEach { sink ->
      runSafely { sink.report(e) }
    }
  }

  fun addSink(sink: Sink) {
    sinks = sinks + sink
  }

  inline fun runSafely(block: () -> Unit) {
    try {
      block()
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

  interface Sink {
    fun dropBreadcrumb(breadcrumb: Breadcrumb)

    fun report(throwable: Throwable)
  }
}
