package org.simple.clinic

import android.app.Application
import com.gabrielittner.threetenbp.LazyThreeTen
import io.reactivex.schedulers.Schedulers
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import org.simple.clinic.analytics.UpdateAnalyticsUserId
import org.simple.clinic.di.AppComponent
import javax.inject.Inject

abstract class ClinicApp : Application() {

  companion object {
    lateinit var appComponent: AppComponent
  }

  // Gets injected in the actual implementations of the ClinicApp
  @Inject
  lateinit var updateAnalyticsUserId: UpdateAnalyticsUserId

  override fun onCreate() {
    super.onCreate()

    @Suppress("ConstantConditionIf")
    if (BuildConfig.API_ENDPOINT == "null") {
      throw AssertionError("API endpoint cannot be null!")
    }

    LazyThreeTen.init(this)

    appComponent = buildDaggerGraph()

    Sentry.init(AndroidSentryClientFactory(applicationContext))
  }

  abstract fun buildDaggerGraph(): AppComponent

  protected fun keepUserIdUpdatedInAnalytics() {
    updateAnalyticsUserId.listen(Schedulers.io())
  }
}
