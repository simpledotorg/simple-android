package org.simple.clinic

import android.app.Application
import android.arch.persistence.room.Room
import io.reactivex.Single
import org.simple.clinic.TestClinicApp.Companion.appComponent
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerTestAppComponent
import org.simple.clinic.di.TestAppComponent
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncModule
import org.simple.clinic.sync.SyncScheduler
import org.threeten.bp.Duration
import javax.inject.Inject

/**
 * This application class makes it possible to inject Android tests with their dependencies.
 * Using [appComponent] in a test's @Before function is a good place to start.
 */
class TestClinicApp : ClinicApp() {

  @Inject
  lateinit var syncScheduler: SyncScheduler

  override fun onCreate() {
    super.onCreate()
    appComponent().inject(this)
    syncScheduler.cancelAll()
  }

  companion object {
    fun appComponent(): TestAppComponent {
      return ClinicApp.appComponent as TestAppComponent
    }
  }

  override fun buildDaggerGraph(): AppComponent {
    return DaggerTestAppComponent.builder()
        .appModule(object : AppModule(this) {
          override fun appDatabase(appContext: Application): AppDatabase {
            return Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
          }
        })
        .syncModule(object : SyncModule() {
          override fun syncConfig(): Single<SyncConfig> {
            return Single.just(SyncConfig(frequency = Duration.ofHours(1), batchSize = 10))
          }
        })
        .build()
  }
}
