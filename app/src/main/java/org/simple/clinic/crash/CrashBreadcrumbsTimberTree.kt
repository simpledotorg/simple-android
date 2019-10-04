package org.simple.clinic.crash

import android.util.Log
import org.simple.clinic.crash.Breadcrumb.Priority.ASSERT
import org.simple.clinic.crash.Breadcrumb.Priority.DEBUG
import org.simple.clinic.crash.Breadcrumb.Priority.ERROR
import org.simple.clinic.crash.Breadcrumb.Priority.INFO
import org.simple.clinic.crash.Breadcrumb.Priority.VERBOSE
import org.simple.clinic.crash.Breadcrumb.Priority.WARN
import timber.log.Timber

class CrashBreadcrumbsTimberTree(
    private val crashReporter: CrashReporter,
    private val priorityToReport: Breadcrumb.Priority = INFO
) : Timber.Tree() {

  override fun log(priority: Int, tag: String?, message: String, error: Throwable?) {
    val breadcrumbPriority = mapLogPriorityToBreadcrumbPriority(priority)

    if (breadcrumbPriority >= priorityToReport) {
      val breadcrumbMessage = mergeMessageWithError(message, error)

      val breadcrumb = Breadcrumb(
          priority = breadcrumbPriority,
          tag = tag,
          message = breadcrumbMessage
      )
      crashReporter.dropBreadcrumb(breadcrumb)
    }
  }

  private fun mergeMessageWithError(message: String, error: Throwable?): String {
    return when (error) {
      null -> message
      else -> "$message (error: ${error.message})"
    }
  }

  private fun mapLogPriorityToBreadcrumbPriority(priority: Int): Breadcrumb.Priority {
    return when (priority) {
      Log.VERBOSE -> VERBOSE
      Log.DEBUG -> DEBUG
      Log.INFO -> INFO
      Log.WARN -> WARN
      Log.ERROR -> ERROR
      Log.ASSERT -> ASSERT
      else -> VERBOSE
    }
  }
}
