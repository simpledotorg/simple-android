package org.simple.clinic

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.test.platform.app.InstrumentationRegistry
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.core.configuration.UploadFrequency
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.RumMonitor
import com.datadog.android.rum.tracking.ViewTrackingStrategy
import com.datadog.android.tracing.AndroidTracer
import com.tspoon.traceur.Traceur
import io.opentracing.util.GlobalTracer
import org.simple.clinic.TestClinicApp.Companion.appComponent
import org.simple.clinic.di.DaggerTestAppComponent
import org.simple.clinic.di.TestAppComponent
import org.simple.clinic.di.TestAppModule
import timber.log.Timber

/**
 * This application class makes it possible to inject Android tests with their dependencies.
 * Using [appComponent] in a test's @Before function is a good place to start.
 */
class TestClinicApp : Application() {

  companion object {
    private lateinit var appComponent: TestAppComponent

    var isInBenchmarkMode = false

    fun appComponent(): TestAppComponent {
      return appComponent
    }
  }

  override fun onCreate() {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
    Traceur.enableLogging()

    appComponent = buildDaggerGraph()
    ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
      override fun executeOnDiskIO(runnable: Runnable) {
        runnable.run()
      }

      override fun postToMainThread(runnable: Runnable) {
        runnable.run()
      }

      override fun isMainThread() = true
    })

    val instrumentationArgs = InstrumentationRegistry.getArguments()
    isInBenchmarkMode = instrumentationArgs.getString("benchmark_app_performance", "false").toBooleanStrict()
    if (isInBenchmarkMode) {
      setupDatadog(
          clientToken = instrumentationArgs.getString("dd_client_token")!!,
          applicationId = instrumentationArgs.getString("dd_application_id")!!
      )
    }
  }

  private fun buildDaggerGraph(): TestAppComponent {
    return DaggerTestAppComponent.builder()
        .testAppModule(TestAppModule(this))
        .build()
  }

  private fun setupDatadog(
      clientToken: String,
      applicationId: String
  ) {
    val datadogConfig = Configuration
        .Builder(
            logsEnabled = true,
            tracesEnabled = true,
            crashReportsEnabled = false,
            rumEnabled = false
        )
        .useViewTrackingStrategy(NoopViewTrackingStrategy())
        .setUploadFrequency(UploadFrequency.FREQUENT)
        .build()
    val credentials = Credentials(
        clientToken = clientToken,
        envName = "test",
        variant = BuildConfig.FLAVOR,
        rumApplicationId = applicationId,
        serviceName = "simple-android-perf-regression"
    )
    Datadog.initialize(this, credentials, datadogConfig, TrackingConsent.GRANTED)
    GlobalRum.registerIfAbsent(RumMonitor.Builder().build())
    GlobalTracer.registerIfAbsent(AndroidTracer.Builder().setPartialFlushThreshold(5).build())
  }

  private class NoopViewTrackingStrategy: ViewTrackingStrategy {
    override fun register(context: Context) {
      // No need to track views in tests
    }

    override fun unregister(context: Context?) {
      // No need to track views in tests
    }
  }
}
