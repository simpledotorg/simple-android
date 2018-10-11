package org.simple.clinic

import android.app.Application
import com.gabrielittner.threetenbp.LazyThreeTen
import com.tspoon.traceur.Traceur
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.analytics.UpdateAnalyticsUserId
import org.simple.clinic.crash.CrashBreadcrumbsTimberTree
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.di.AppComponent
import timber.log.Timber
import javax.inject.Inject

abstract class ClinicApp : Application() {

  companion object {
    lateinit var appComponent: AppComponent
  }

  @Inject
  lateinit var updateAnalyticsUserId: UpdateAnalyticsUserId

  @Inject
  lateinit var crashReporter: CrashReporter

  override fun onCreate() {
    super.onCreate()

    @Suppress("ConstantConditionIf")
    if (BuildConfig.API_ENDPOINT == "null") {
      throw AssertionError("API endpoint cannot be null!")
    }

    Traceur.enableLogging()
    LazyThreeTen.init(this)

    appComponent = buildDaggerGraph()
    appComponent.inject(this)

    crashReporter.init(this)
    Timber.plant(CrashBreadcrumbsTimberTree(crashReporter))
  }

  abstract fun buildDaggerGraph(): AppComponent

  protected fun keepUserIdUpdatedInAnalytics() {
    updateAnalyticsUserId.listen(Schedulers.io())
  }
}
