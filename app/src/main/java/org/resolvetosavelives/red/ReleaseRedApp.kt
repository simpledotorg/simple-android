package org.resolvetosavelives.red

import org.resolvetosavelives.red.di.AppComponent
import org.resolvetosavelives.red.di.AppModule
import org.resolvetosavelives.red.di.DaggerAppComponent
import org.resolvetosavelives.red.sync.PatientSyncScheduler
import javax.inject.Inject

class ReleaseRedApp : RedApp() {

  @Inject
  lateinit var syncScheduler: PatientSyncScheduler

  override fun onCreate() {
    super.onCreate()

    appComponent.inject(this)
    syncScheduler.schedule().subscribe()
  }

  override fun buildDaggerGraph(): AppComponent {
    return DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()
  }
}
