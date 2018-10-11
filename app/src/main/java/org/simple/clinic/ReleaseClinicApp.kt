package org.simple.clinic

import android.annotation.SuppressLint
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.HeapAnalyticsReporter
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerAppComponent
import org.simple.clinic.sync.SyncScheduler
import javax.inject.Inject

@SuppressLint("Registered")
class ReleaseClinicApp : ClinicApp() {

  @Inject
  lateinit var syncScheduler: SyncScheduler

  override fun onCreate() {
    super.onCreate()
    appComponent.inject(this)
    Analytics.addReporter(HeapAnalyticsReporter(this))
    keepUserIdUpdatedInAnalytics()
    syncScheduler.schedule().subscribe()
  }

  override fun buildDaggerGraph(): AppComponent {
    return DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()
  }
}
