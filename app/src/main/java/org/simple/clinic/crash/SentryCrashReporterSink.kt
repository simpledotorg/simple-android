package org.simple.clinic.crash

import io.sentry.Sentry
import io.sentry.SentryLevel
import org.simple.clinic.platform.crash.Breadcrumb
import org.simple.clinic.platform.crash.Breadcrumb.Priority.ASSERT
import org.simple.clinic.platform.crash.Breadcrumb.Priority.DEBUG
import org.simple.clinic.platform.crash.Breadcrumb.Priority.ERROR
import org.simple.clinic.platform.crash.Breadcrumb.Priority.INFO
import org.simple.clinic.platform.crash.Breadcrumb.Priority.VERBOSE
import org.simple.clinic.platform.crash.Breadcrumb.Priority.WARN
import org.simple.clinic.platform.crash.CrashReporter
import javax.inject.Inject
import io.sentry.Breadcrumb as SentryBreadCrumb

typealias SentryBreadcrumbLevel = SentryLevel

class SentryCrashReporterSink @Inject constructor() : CrashReporter.Sink {

  override fun dropBreadcrumb(breadcrumb: Breadcrumb) {
    val sentryBreadcrumb = SentryBreadCrumb().apply {
      level = priorityToLevel(breadcrumb.priority)
      category = breadcrumb.tag
      message = breadcrumb.message
    }

    Sentry.addBreadcrumb(sentryBreadcrumb)
  }

  private fun priorityToLevel(priority: Breadcrumb.Priority): SentryBreadcrumbLevel {
    return when (priority) {
      VERBOSE, DEBUG -> SentryBreadcrumbLevel.DEBUG
      INFO -> SentryBreadcrumbLevel.INFO
      WARN -> SentryBreadcrumbLevel.WARNING
      ERROR, ASSERT -> SentryBreadcrumbLevel.ERROR
    }
  }

  override fun report(throwable: Throwable) {
    Sentry.captureException(throwable)
  }
}
