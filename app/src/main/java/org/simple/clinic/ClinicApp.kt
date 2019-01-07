package org.simple.clinic

import android.app.Application
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

  override fun onCreate() {
    super.onCreate()

    @Suppress("ConstantConditionIf")
    if (BuildConfig.API_ENDPOINT == "null") {
      throw AssertionError("API endpoint cannot be null!")
    }

    Traceur.enableLogging()
    WorkManager.initialize(this, Configuration.Builder().build())
    LazyThreeTen.init(this)

    appComponent = buildDaggerGraph()
    appComponent.inject(this)

    crashReporter.init(this)
    Timber.plant(CrashBreadcrumbsTimberTree(crashReporter))

    updateAnalyticsUserId.listen(Schedulers.io())
    syncProtocolsOnLogin.listen()
  }

  abstract fun buildDaggerGraph(): AppComponent

}
