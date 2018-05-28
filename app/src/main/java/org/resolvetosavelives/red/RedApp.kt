package org.resolvetosavelives.red

import android.app.Application
import com.gabrielittner.threetenbp.LazyThreeTen
import org.resolvetosavelives.red.di.AppComponent
import org.resolvetosavelives.red.di.AppModule
import org.resolvetosavelives.red.di.DaggerAppComponent
import org.resolvetosavelives.red.sync.PatientSyncScheduler
import timber.log.Timber
import javax.inject.Inject

class RedApp : Application() {

  companion object {
    lateinit var appComponent: AppComponent
  }

  @Inject
  lateinit var syncScheduler: PatientSyncScheduler

  override fun onCreate() {
    super.onCreate()

    appComponent = DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()
    appComponent.inject(this)

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    LazyThreeTen.init(this)

    syncScheduler.schedule().subscribe()
  }
}
