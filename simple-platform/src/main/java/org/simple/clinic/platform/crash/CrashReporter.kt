package org.simple.clinic.platform.crash

import android.app.Application
import timber.log.Timber

object CrashReporter : CrashReporter_Old {

  private var sinks = emptyList<Sink>()

  override fun init(appContext: Application) {
    // We will leave initialization to the individual sink constructor implementation
    // since we do not know their requirements
    // TODO: Remove this method in a later commit
  }

  override fun dropBreadcrumb(breadcrumb: Breadcrumb) {
    sinks.forEach { sink ->
      runSafely { sink.dropBreadcrumb(breadcrumb) }
    }
  }

  override fun report(e: Throwable) {
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
