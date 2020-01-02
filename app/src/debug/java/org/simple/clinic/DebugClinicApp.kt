package org.simple.clinic

import android.annotation.SuppressLint
import android.app.Activity
import android.os.StrictMode
import com.facebook.stetho.Stetho
import com.tspoon.traceur.Traceur
import io.github.inflationx.viewpump.ViewPump
import org.simple.clinic.activity.SimpleActivityLifecycleCallbacks
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerDebugAppComponent
import org.simple.clinic.di.DebugAppComponent
import org.simple.clinic.main.TheActivity
import org.simple.clinic.util.AppSignature
import org.simple.clinic.widgets.ProxySystemKeyboardEnterToImeOption
import timber.log.Timber

@SuppressLint("Registered")
class DebugClinicApp : ClinicApp() {

  private lateinit var signature: AppSignature

  companion object {
    fun appComponent(): DebugAppComponent {
      return appComponent as DebugAppComponent
    }
  }

  override fun onCreate() {
    addStrictModeChecks()
    Traceur.enableLogging()
    super.onCreate()

    appComponent().inject(this)

    Timber.plant(Timber.DebugTree())
    Stetho.initializeWithDefaults(this)
    showDebugNotification()

    ViewPump.init(ViewPump.builder()
        .addInterceptor(ProxySystemKeyboardEnterToImeOption())
        .build())

    signature = AppSignature(this)
  }

  private fun showDebugNotification() {
    registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
      override fun onActivityStarted(activity: Activity) {
        if (activity is TheActivity) {
          DebugNotification.show(activity, signature.appSignatures)
        }
      }

      override fun onActivityStopped(activity: Activity) {
        if (activity is TheActivity) {
          DebugNotification.stop(activity)
        }
      }
    })
  }

  override fun buildDaggerGraph(): AppComponent {
    return DaggerDebugAppComponent.builder()
        .appModule(AppModule(this))
        .build()
  }

  private fun addStrictModeChecks() {
    StrictMode
        .setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeathOnNetwork()
                .build()
        )
    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog()
            .penaltyDeath()
            .build()
    )
  }
}
