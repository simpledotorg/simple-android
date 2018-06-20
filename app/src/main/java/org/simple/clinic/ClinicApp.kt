package org.simple.clinic

import android.support.multidex.MultiDexApplication
import com.gabrielittner.threetenbp.LazyThreeTen
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import org.simple.clinic.di.AppComponent

abstract class ClinicApp : MultiDexApplication() {

  companion object {
    lateinit var appComponent: AppComponent
  }

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
}
