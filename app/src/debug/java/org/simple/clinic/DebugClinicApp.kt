package org.simple.clinic

import android.annotation.SuppressLint
import com.facebook.stetho.Stetho
import com.tspoon.traceur.Traceur
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerAppComponent
import org.simple.clinic.sync.SyncScheduler
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("Registered")
class DebugClinicApp : ClinicApp() {

  @Inject
  lateinit var syncScheduler: SyncScheduler

  override fun onCreate() {
    super.onCreate()

    Timber.plant(Timber.DebugTree())
    Traceur.enableLogging()
    Stetho.initializeWithDefaults(this)
    syncScheduler.schedule().subscribe()
  }

  override fun buildDaggerGraph(): AppComponent {
    return DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()
  }
}
