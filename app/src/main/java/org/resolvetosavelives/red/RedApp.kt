package org.resolvetosavelives.red

import android.app.Application
import com.facebook.stetho.Stetho
import com.gabrielittner.threetenbp.LazyThreeTen
import com.tspoon.traceur.Traceur
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import org.resolvetosavelives.red.di.AppComponent
import timber.log.Timber

abstract class RedApp : Application() {

  val dsn = "https://e8606e0cd2f7470e834e446905612ff2@sentry.io/1212614"

  companion object {
    lateinit var appComponent: AppComponent
  }

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
      Traceur.enableLogging()
      Stetho.initializeWithDefaults(this)
    }

    LazyThreeTen.init(this)

    appComponent = buildDaggerGraph()

    Sentry.init(dsn, AndroidSentryClientFactory(this))
  }

  abstract fun buildDaggerGraph(): AppComponent
}
