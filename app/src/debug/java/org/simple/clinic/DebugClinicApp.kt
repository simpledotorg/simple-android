package org.simple.clinic

import android.annotation.SuppressLint
import android.app.Activity
import com.facebook.stetho.Stetho
import com.tspoon.traceur.Traceur
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerDebugAppComponent
import org.simple.clinic.di.DebugAppComponent
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.widgets.SimpleActivityLifecycleCallbacks
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("Registered")
class DebugClinicApp : ClinicApp() {

  @Inject
  lateinit var syncScheduler: SyncScheduler

  companion object {
    fun appComponent(): DebugAppComponent {
      return ClinicApp.appComponent as DebugAppComponent
    }
  }

  override fun onCreate() {
    super.onCreate()
    appComponent().inject(this)

    Timber.plant(Timber.DebugTree())
    Traceur.enableLogging()
    Stetho.initializeWithDefaults(this)

    syncScheduler.schedule().subscribe()

    registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
      override fun onActivityStarted(activity: Activity) {
        if (activity is TheActivity) {
          DebugNotificationService.show(activity)
        }
      }

      override fun onActivityStopped(activity: Activity) {
        if (activity is TheActivity) {
          DebugNotificationService.stop(activity)
        }
      }
    })
  }

  override fun buildDaggerGraph(): AppComponent {
    return DaggerDebugAppComponent.builder()
        .appModule(AppModule(this))
        .build()
  }
}
