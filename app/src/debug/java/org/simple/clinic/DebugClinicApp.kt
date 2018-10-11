package org.simple.clinic

import android.annotation.SuppressLint
import android.app.Activity
import com.facebook.stetho.Stetho
import dagger.Provides
import io.github.inflationx.viewpump.ViewPump
import io.reactivex.Single
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.DebugAnalyticsReporter
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerDebugAppComponent
import org.simple.clinic.di.DebugAppComponent
import org.simple.clinic.login.LoginModule
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.registration.RegistrationConfig
import org.simple.clinic.registration.RegistrationModule
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.util.AppSignature
import org.simple.clinic.widgets.ProxySystemKeyboardEnterToImeOption
import org.simple.clinic.widgets.SimpleActivityLifecycleCallbacks
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("Registered")
class DebugClinicApp : ClinicApp() {

  @Inject
  lateinit var syncScheduler: SyncScheduler

  private lateinit var signature: AppSignature

  companion object {
    fun appComponent(): DebugAppComponent {
      return ClinicApp.appComponent as DebugAppComponent
    }
  }

  override fun onCreate() {
    super.onCreate()
    appComponent().inject(this)
    Analytics.addReporter(DebugAnalyticsReporter())
    keepUserIdUpdatedInAnalytics()

    Timber.plant(Timber.DebugTree())
    Stetho.initializeWithDefaults(this)
    syncScheduler.schedule().subscribe()
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
        .loginModule(object : LoginModule() {
          override fun appLockConfig(): Single<AppLockConfig> {
            return Single.just(AppLockConfig(lockAfterTimeMillis = TimeUnit.SECONDS.toMillis(4)))
          }
        })
        .registrationModule(object : RegistrationModule() {
          @Provides
          override fun registrationConfig(): Single<RegistrationConfig> {
            return Single.just(RegistrationConfig(retryBackOffDelayInMinutes = 1))
          }
        })
        .build()
  }
}
