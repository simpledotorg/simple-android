package org.simple.clinic

import android.annotation.SuppressLint
import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.RumMonitor
import com.datadog.android.rum.tracking.FragmentViewTrackingStrategy
import com.datadog.android.tracing.AndroidTracer
import io.opentracing.util.GlobalTracer
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import org.simple.clinic.activity.CloseActivitiesWhenUserIsUnauthorized
import org.simple.clinic.analytics.ResolveScreenNamesForDatadog
import org.simple.clinic.analytics.UpdateAnalyticsUserId
import org.simple.clinic.crash.CrashBreadcrumbsTimberTree
import org.simple.clinic.crash.SentryCrashReporterSink
import org.simple.clinic.di.AppComponent
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.AnalyticsReporter
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.storage.monitoring.AnalyticsSqlPerformanceReportingSink
import org.simple.clinic.storage.monitoring.DatadogSqlPerformanceReportingSink
import org.simple.clinic.storage.monitoring.SqlPerformanceReporter
import org.simple.clinic.util.clamp
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
  lateinit var analyticsSqlPerformanceReportingSink: AnalyticsSqlPerformanceReportingSink

  @Inject
  lateinit var sentryCrashReporterSink: SentryCrashReporterSink

  @Inject
  lateinit var remoteConfig: ConfigReader

  protected open val analyticsReporters = emptyList<AnalyticsReporter>()

  protected open val crashReporterSinks = emptyList<CrashReporter.Sink>()

  @SuppressLint("RestrictedApi")
  override fun onCreate() {
    super.onCreate()

    appComponent = buildDaggerGraph()
    appComponent.inject(this)

    crashReporterSinks.forEach(CrashReporter::addSink)

    setupApplicationPerformanceMonitoring()

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
    SqlPerformanceReporter.addSink(analyticsSqlPerformanceReportingSink)
    SqlPerformanceReporter.addSink(DatadogSqlPerformanceReportingSink())

    updateAnalyticsUserId.listen()

    registerActivityLifecycleCallbacks(closeActivitiesWhenUserIsUnauthorized)
    closeActivitiesWhenUserIsUnauthorized.listen()
  }

  private fun setupApplicationPerformanceMonitoring() {
    val samplingRate = remoteConfig
        .double("datadog_sample_rate", 0.0)
        .toFloat()
        .clamp(0F, 100F)

    val datadogConfig = Configuration
        .Builder(
            logsEnabled = false,
            tracesEnabled = true,
            crashReportsEnabled = false,
            rumEnabled = true
        )
        .trackBackgroundRumEvents(true)
        .trackLongTasks(5000)
        .useViewTrackingStrategy(FragmentViewTrackingStrategy(
            trackArguments = false,
            supportFragmentComponentPredicate = ResolveScreenNamesForDatadog()
        ))
        .sampleRumSessions(samplingRate = samplingRate)
        .build()
    val credentials = Credentials(
        clientToken = BuildConfig.DATADOG_CLIENT_TOKEN,
        envName = BuildConfig.DATADOG_ENVIRONMENT,
        variant = BuildConfig.FLAVOR,
        rumApplicationId = BuildConfig.DATADOG_APPLICATION_ID,
        serviceName = BuildConfig.DATADOG_SERVICE_NAME
    )
    Datadog.initialize(this, credentials, datadogConfig, TrackingConsent.GRANTED)
    GlobalRum.registerIfAbsent(RumMonitor.Builder().build())
    GlobalTracer.registerIfAbsent(AndroidTracer.Builder().build())
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
