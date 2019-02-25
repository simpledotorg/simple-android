package org.simple.clinic

import android.annotation.SuppressLint
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.HeapAnalyticsReporter
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerAppComponent
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.sync.indicator.SyncIndicatorStatusCalculator
import javax.inject.Inject

@SuppressLint("Registered")
class ReleaseClinicApp : ClinicApp() {

  @Inject
  lateinit var syncScheduler: SyncScheduler

  @Inject
  lateinit var syncIndicatorStatusCalculator: SyncIndicatorStatusCalculator

  override fun onCreate() {
    super.onCreate()
    appComponent.inject(this)
    Analytics.addReporter(HeapAnalyticsReporter(this))
    syncScheduler.schedule().subscribe()
    syncIndicatorStatusCalculator.updateSyncResults()
  }

  override fun buildDaggerGraph(): AppComponent {
    return DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()
  }
}
