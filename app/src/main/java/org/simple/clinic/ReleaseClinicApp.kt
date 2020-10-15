package org.simple.clinic

import android.annotation.SuppressLint
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.simple.clinic.analytics.MixpanelAnalyticsReporter
import org.simple.clinic.analytics.swallowErrors
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerAppComponent
import org.simple.clinic.util.unsafeLazy
import org.slf4j.LoggerFactory

@SuppressLint("Registered")
class ReleaseClinicApp : ClinicApp() {

  override val analyticsReporters by unsafeLazy {
    listOf(MixpanelAnalyticsReporter(this).swallowErrors())
  }

  override fun onCreate() {
    super.onCreate()

    // Since it's possible to log sensitive information
    // present in Mobius model & events. We are turning off the logs
    // for `ControllerStateBase` where we log those.
    val logger = LoggerFactory.getLogger("com.spotify.mobius.ControllerStateBase") as Logger
    logger.level = Level.OFF
  }

  override fun buildDaggerGraph(): AppComponent {
    return DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()
  }
}
