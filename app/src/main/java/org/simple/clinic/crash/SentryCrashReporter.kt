package org.simple.clinic.crash

import android.app.Application
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import io.sentry.event.BreadcrumbBuilder
import org.simple.clinic.crash.Breadcrumb.Priority.ASSERT
import org.simple.clinic.crash.Breadcrumb.Priority.DEBUG
import org.simple.clinic.crash.Breadcrumb.Priority.ERROR
import org.simple.clinic.crash.Breadcrumb.Priority.INFO
import org.simple.clinic.crash.Breadcrumb.Priority.VERBOSE
import org.simple.clinic.crash.Breadcrumb.Priority.WARN
import java.util.Date

typealias SentryBreadcrumbLevel = io.sentry.event.Breadcrumb.Level

class SentryCrashReporter : CrashReporter {

  override fun init(appContext: Application) {
    Sentry.init(AndroidSentryClientFactory(appContext))
  }

  override fun dropBreadcrumb(breadcrumb: Breadcrumb) {
    val sentryBreadcrumb = BreadcrumbBuilder()
        .setLevel(priorityToLevel(breadcrumb.priority))
        .setCategory(breadcrumb.tag)
        .setMessage(breadcrumb.message)
        .setTimestamp(Date(System.currentTimeMillis()))
        .build()
    Sentry.getContext().recordBreadcrumb(sentryBreadcrumb)
  }

  private fun priorityToLevel(priority: Breadcrumb.Priority): SentryBreadcrumbLevel {
    return when (priority) {
      VERBOSE, DEBUG -> SentryBreadcrumbLevel.DEBUG
      INFO -> SentryBreadcrumbLevel.INFO
      WARN -> SentryBreadcrumbLevel.WARNING
      ERROR, ASSERT -> SentryBreadcrumbLevel.ERROR
    }
  }

  override fun report(e: Throwable) {
    Sentry.capture(e)
  }
}
