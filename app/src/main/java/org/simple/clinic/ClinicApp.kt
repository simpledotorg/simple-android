package org.simple.clinic

import android.annotation.SuppressLint
import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.sentry.SentryLevel
import io.sentry.SentryOptions.BeforeSendCallback
import io.sentry.android.core.SentryAndroid
import io.sentry.android.fragment.FragmentLifecycleIntegration
import org.simple.clinic.activity.CloseActivitiesWhenUserIsUnauthorized
import org.simple.clinic.analytics.UpdateAnalyticsUserId
import org.simple.clinic.crash.CrashBreadcrumbsTimberTree
import org.simple.clinic.crash.SentryCrashReporterSink
import org.simple.clinic.di.AppComponent
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.AnalyticsReporter
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.plumbing.infrastructure.UpdateInfrastructureUserDetails
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.remoteconfig.UpdateFacilityRemoteConfig
import org.simple.clinic.storage.DatabaseEncryptor
import org.simple.clinic.storage.monitoring.SentrySqlPerformanceReportingSink
import org.simple.clinic.storage.monitoring.SqlPerformanceReporter
import org.simple.clinic.util.scheduler.SchedulersProvider
import timber.log.Timber
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject

abstract class ClinicApp : Application(), CameraXConfig.Provider {

  companion object {
    lateinit var appComponent: AppComponent
  }

  @Inject
  lateinit var updateAnalyticsUserId: UpdateAnalyticsUserId

  @Inject
  lateinit var closeActivitiesWhenUserIsUnauthorized: CloseActivitiesWhenUserIsUnauthorized

  @Inject
  lateinit var sentryCrashReporterSink: SentryCrashReporterSink

  @Inject
  lateinit var remoteConfig: ConfigReader

  @Inject
  lateinit var updateInfrastructureUserDetails: UpdateInfrastructureUserDetails

  @Inject
  lateinit var databaseEncryptor: DatabaseEncryptor

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var updateFacilityRemoteConfig: UpdateFacilityRemoteConfig

  protected open val analyticsReporters = emptyList<AnalyticsReporter>()

  protected open val crashReporterSinks = emptyList<CrashReporter.Sink>()

  @SuppressLint("RestrictedApi", "CheckResult")
  override fun onCreate() {
    super.onCreate()

    appComponent = buildDaggerGraph()
    appComponent.inject(this)

    setupSentry()

    databaseEncryptor
        .databaseEncryptionState
        .filter {
          it == DatabaseEncryptor.State.SKIPPED ||
              it == DatabaseEncryptor.State.ENCRYPTED
        }
        .subscribeOn(schedulersProvider.ui())
        .subscribe {
          updateInfrastructureUserDetails.track()
          updateAnalyticsUserId.listen()
          closeActivitiesWhenUserIsUnauthorized.listen()
          updateFacilityRemoteConfig.track()
        }

    crashReporterSinks.forEach(CrashReporter::addSink)

    Timber.plant(CrashBreadcrumbsTimberTree())
    RxJavaPlugins.setErrorHandler { error ->
      if (!error.canBeIgnoredSafely()) {
        val cause = if (error is UndeliverableException) error.cause else error
        Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), cause!!)
      }
    }

    analyticsReporters.forEach { reporter ->
      Analytics.addReporter(reporter)
    }
    SqlPerformanceReporter.addSink(SentrySqlPerformanceReportingSink())

    registerActivityLifecycleCallbacks(closeActivitiesWhenUserIsUnauthorized)
  }

  private fun setupSentry() {
    SentryAndroid.init(this) { options ->
      options.dsn = BuildConfig.SENTRY_DSN
      options.environment = BuildConfig.SENTRY_ENVIRONMENT

      options.sampleRate = remoteConfig
          .double("sentry_errors_sample_rate", 0.0)
          .coerceIn(0.0, 1.0)
      options.tracesSampleRate = remoteConfig
          .double("sentry_traces_sample_rate", 0.0)
          .coerceIn(0.0, 1.0)

      options.beforeSend = BeforeSendCallback { event, hint ->
        if (event.level != SentryLevel.DEBUG) {
          event
        } else {
          null
        }
      }

      options.addIntegration(
          FragmentLifecycleIntegration(
              application = this,
              enableFragmentLifecycleBreadcrumbs = true,
              enableAutoFragmentLifecycleTracing = true,
          )
      )
    }
  }

  override fun getCameraXConfig(): CameraXConfig {
    return Camera2Config.defaultConfig()
  }

  abstract fun buildDaggerGraph(): AppComponent
}

private fun Throwable.canBeIgnoredSafely(): Boolean {
  return this is UndeliverableException && when (cause) {
    // Irrelevant network problem or API that throws on cancellation
    is IOException, is SocketException -> true
    // Some blocking code was interrupted by a dispose call
    is InterruptedException -> true
    else -> false
  }
}
