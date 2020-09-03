package org.simple.clinic

import android.annotation.SuppressLint
import android.app.Application
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.work.Configuration
import androidx.work.WorkManager
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import org.simple.clinic.activity.CloseActivitiesWhenUserIsUnauthorized
import org.simple.clinic.analytics.UpdateAnalyticsUserId
import org.simple.clinic.crash.CrashBreadcrumbsTimberTree
import org.simple.clinic.di.AppComponent
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.AnalyticsReporter
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.util.AppArchTaskExecutorDelegate
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
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var closeActivitiesWhenUserIsUnauthorized: CloseActivitiesWhenUserIsUnauthorized

  protected open val analyticsReporters = emptyList<AnalyticsReporter>()

  @SuppressLint("RestrictedApi")
  override fun onCreate() {
    super.onCreate()
    // Room uses the architecture components executor for doing IO work,
    // which is limited to two threads. This causes thread starvation in some
    // cases, especially when syncs are ongoing. This changes the thread pool
    // to a cached thread pool, which will create and reuse threads when
    // necessary.
    ArchTaskExecutor.getInstance().setDelegate(AppArchTaskExecutorDelegate())
    WorkManager.initialize(this, Configuration.Builder().build())

    appComponent = buildDaggerGraph()
    appComponent.inject(this)

    crashReporter.init(this)
    Timber.plant(CrashBreadcrumbsTimberTree(crashReporter))
    RxJavaPlugins.setErrorHandler { error ->
      if (!error.canBeIgnoredSafely()) {
        val cause = if(error is UndeliverableException) error.cause else error
        Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), cause!!)
      }
    }

    analyticsReporters.forEach { reporter ->
      Analytics.addReporter(reporter)
    }

    updateAnalyticsUserId.listen()

    registerActivityLifecycleCallbacks(closeActivitiesWhenUserIsUnauthorized)
    closeActivitiesWhenUserIsUnauthorized.listen()
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
