package org.simple.clinic

import android.annotation.SuppressLint
import org.simple.clinic.analytics.HeapAnalyticsReporter
import org.simple.clinic.analytics.MixpanelAnalyticsReporter
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerAppComponent
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.sync.indicator.SyncIndicatorStatusCalculator
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

@SuppressLint("Registered")
class ReleaseClinicApp : ClinicApp() {

  @Inject
  lateinit var syncScheduler: SyncScheduler

  @Inject
  lateinit var syncIndicatorStatusCalculator: SyncIndicatorStatusCalculator

  override val analyticsReporters by unsafeLazy {
    listOf(
        HeapAnalyticsReporter(this),
        MixpanelAnalyticsReporter(this)
    )
  }

  override fun onCreate() {
    super.onCreate()
    appComponent.inject(this)
    syncScheduler.schedule().subscribe()
    syncIndicatorStatusCalculator.updateSyncResults()
  }

  override fun buildDaggerGraph(): AppComponent {
    return DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()
  }
}
