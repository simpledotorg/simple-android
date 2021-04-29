package org.simple.clinic.crash

import android.annotation.SuppressLint
import android.app.Application
import io.reactivex.schedulers.Schedulers.io
import io.sentry.Sentry
import io.sentry.SentryLevel
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.platform.crash.Breadcrumb
import org.simple.clinic.platform.crash.Breadcrumb.Priority.ASSERT
import org.simple.clinic.platform.crash.Breadcrumb.Priority.DEBUG
import org.simple.clinic.platform.crash.Breadcrumb.Priority.ERROR
import org.simple.clinic.platform.crash.Breadcrumb.Priority.INFO
import org.simple.clinic.platform.crash.Breadcrumb.Priority.VERBOSE
import org.simple.clinic.platform.crash.Breadcrumb.Priority.WARN
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.extractIfPresent
import javax.inject.Inject
import io.sentry.Breadcrumb as SentryBreadCrumb

typealias SentryBreadcrumbLevel = SentryLevel

class SentryCrashReporter @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val appConfigRepository: AppConfigRepository
) : CrashReporter {

  override fun init(appContext: Application) {
    identifyUserAndCurrentFacility()
    identifyCurrentCountryCode()
  }

  @Suppress("CheckResult")
  private fun identifyUserAndCurrentFacility() {
    val loggedInUserStream = userSession.loggedInUser()
        .subscribeOn(io())
        .extractIfPresent()
        .replay()
        .refCount()

    loggedInUserStream
        .map { it.uuid }
        .subscribe(
            { Sentry.setTag("userUuid", it.toString()) },
            { report(it) })

    facilityRepository
        .currentFacility()
        .map { it.uuid }
        .subscribe(
            { Sentry.setTag("facilityUuid", it.toString()) },
            { report(it) })
  }

  @SuppressLint("CheckResult")
  private fun identifyCurrentCountryCode() {
    appConfigRepository
        .currentCountryObservable()
        .extractIfPresent()
        .map { it.isoCountryCode }
        .subscribe(
            {
              Sentry.setTag("countryCode", it.toString())
            },
            { report(it) })
  }

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

  override fun report(e: Throwable) {
    Sentry.captureException(e)
  }
}
