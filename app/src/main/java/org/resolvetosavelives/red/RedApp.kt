package org.resolvetosavelives.red

import android.app.Application
import org.resolvetosavelives.red.di.AppComponent
import org.resolvetosavelives.red.di.AppModule
import org.resolvetosavelives.red.di.DaggerAppComponent
import timber.log.Timber

class RedApp : Application() {

  companion object {
    lateinit var appComponent: AppComponent
  }

  override fun onCreate() {
    super.onCreate()

    Timber.i("Planing timber tree")
    Timber.plant(Timber.DebugTree())

    appComponent = DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()
  }
}
