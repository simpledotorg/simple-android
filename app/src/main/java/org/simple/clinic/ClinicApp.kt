package org.simple.clinic

import android.support.multidex.MultiDexApplication
import com.gabrielittner.threetenbp.LazyThreeTen
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import org.simple.clinic.di.AppComponent
import org.simple.clinic.util.AppSignature

abstract class ClinicApp : MultiDexApplication() {

  companion object {
    lateinit var appComponent: AppComponent
  }

  protected lateinit var signature: AppSignature

  override fun onCreate() {
    super.onCreate()

    @Suppress("ConstantConditionIf")
    if (BuildConfig.API_ENDPOINT == "null") {
      throw AssertionError("API endpoint cannot be null!")
    }

    LazyThreeTen.init(this)

    appComponent = buildDaggerGraph()

    Sentry.init(AndroidSentryClientFactory(applicationContext))
    signature = AppSignature(this)
  }

  abstract fun buildDaggerGraph(): AppComponent
}
