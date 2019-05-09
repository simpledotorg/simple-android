package org.simple.clinic

import android.annotation.SuppressLint
import android.app.Application
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.work.Configuration
import androidx.work.WorkManager
import com.gabrielittner.threetenbp.LazyThreeTen
import com.tspoon.traceur.Traceur
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.analytics.UpdateAnalyticsUserId
import org.simple.clinic.crash.CrashBreadcrumbsTimberTree
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.di.AppComponent
import org.simple.clinic.protocol.SyncProtocolsOnLogin
import org.simple.clinic.user.UnauthorizeUser
import org.simple.clinic.util.AppArchTaskExecutorDelegate
import timber.log.Timber
import javax.inject.Inject

abstract class ClinicApp : Application() {

  companion object {
    lateinit var appComponent: AppComponent
  }

  @Inject
  lateinit var updateAnalyticsUserId: UpdateAnalyticsUserId

  @Inject
  lateinit var syncProtocolsOnLogin: SyncProtocolsOnLogin

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var unauthorizeUser: UnauthorizeUser

  @SuppressLint("RestrictedApi")
  override fun onCreate() {
    super.onCreate()

    @Suppress("ConstantConditionIf")
    if (BuildConfig.API_ENDPOINT == "null") {
      throw AssertionError("API endpoint cannot be null!")
    }

    Traceur.enableLogging()
    // Room uses the architecture components executor for doing IO work,
    // which is limited to two threads. This causes thread starvation in some
    // cases, especially when syncs are ongoing. This changes the thread pool
    // to a cached thread pool, which will create and reuse threads when
    // necessary.
    ArchTaskExecutor.getInstance().setDelegate(AppArchTaskExecutorDelegate())
    WorkManager.initialize(this, Configuration.Builder().build())
    LazyThreeTen.init(this)

    appComponent = buildDaggerGraph()
    appComponent.inject(this)

    crashReporter.init(this)
    Timber.plant(CrashBreadcrumbsTimberTree(crashReporter))

    updateAnalyticsUserId.listen(Schedulers.io())
    syncProtocolsOnLogin.listen()

//    TODO: Enable this once the feature is complete
//    unauthorizeUser.listen(Schedulers.io())
  }

  abstract fun buildDaggerGraph(): AppComponent

}
