package org.simple.clinic.crash

import android.annotation.SuppressLint
import android.app.Application
import io.reactivex.schedulers.Schedulers.io
import io.sentry.Sentry
import org.simple.clinic.BuildConfig.SENTRY_DSN
import org.simple.clinic.BuildConfig.SENTRY_ENVIRONMENT
import org.simple.clinic.BuildConfig.SENTRY_SAMPLE_RATE
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
import io.sentry.Breadcrumb as SentryBreadcrumb

typealias SentryBreadcrumbLevel = io.sentry.SentryLevel

class SentryCrashReporter @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val appConfigRepository: AppConfigRepository
) : CrashReporter {

  override fun init(appContext: Application) {
    Sentry.init { options ->
      options.dsn = SENTRY_DSN
      options.sampleRate = SENTRY_SAMPLE_RATE
      options.environment = SENTRY_ENVIRONMENT
    }
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
        .map { it.get().isoCountryCode }
        .subscribe(
            {
              Sentry.setTag("countryCode", it.toString())
            },
            { report(it) })
  }

  override fun dropBreadcrumb(breadcrumb: Breadcrumb) {
    val sentryBreadCrumb = SentryBreadcrumb().apply {
      level = priorityToLevel(breadcrumb.priority)
      category = breadcrumb.tag
      message = breadcrumb.message
    }

    Sentry.addBreadcrumb(sentryBreadCrumb)
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
