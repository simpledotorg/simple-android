package org.simple.clinic

import android.annotation.SuppressLint
import android.app.Activity
import android.os.StrictMode
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
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
import javax.inject.Inject

@SuppressLint("Registered")
class DebugClinicApp : ClinicApp() {

  /*
  We are injecting this because we need to share the same instance with the OkHttp interceptor.

  See debug/HttpInterceptorsModule for more info.
  */
  @Inject
  lateinit var networkFlipperPlugin: NetworkFlipperPlugin

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
    SoLoader.init(this, false)

    appComponent().inject(this)

    setupFlipper()

    Timber.plant(Timber.DebugTree())
    showDebugNotification()

    ViewPump.init(ViewPump.builder()
        .addInterceptor(ProxySystemKeyboardEnterToImeOption())
        .build())

    signature = AppSignature(this)
  }

  private fun setupFlipper() {
    val context = this

    with(AndroidFlipperClient.getInstance(this)) {
      addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
      addPlugin(networkFlipperPlugin)

      val databasePlugin = DatabasesFlipperPlugin(ReadOnlySqliteDatabaseDriver(context))
      addPlugin(databasePlugin)

      addPlugin(SharedPreferencesFlipperPlugin(context, "${context.packageName}_preferences"))

      start()
    }
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

    // TODO: Enable `penaltyDeath` once the crash issues are resolved on Android 11
    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog()
            .build()
    )
  }
}
