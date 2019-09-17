package org.simple.clinic

import android.annotation.SuppressLint
import org.simple.clinic.analytics.HeapAnalyticsReporter
import org.simple.clinic.analytics.MixpanelAnalyticsReporter
import org.simple.clinic.analytics.swallowErrors
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerAppComponent
import org.simple.clinic.util.unsafeLazy

@SuppressLint("Registered")
class ReleaseClinicApp : ClinicApp() {

  override val analyticsReporters by unsafeLazy {
    listOf(
        HeapAnalyticsReporter(this).swallowErrors(),
        MixpanelAnalyticsReporter(this).swallowErrors()
    )
  }

  override fun onCreate() {
    super.onCreate()
    setupSync()
  }

  override fun buildDaggerGraph(): AppComponent {
    return DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()
  }
}
