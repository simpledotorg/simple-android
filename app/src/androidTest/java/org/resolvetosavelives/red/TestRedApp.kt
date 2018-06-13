package org.resolvetosavelives.red

import android.app.Application
import android.arch.persistence.room.Room
import io.reactivex.Single
import org.resolvetosavelives.red.TestRedApp.Companion.appComponent
import org.resolvetosavelives.red.di.AppComponent
import org.resolvetosavelives.red.di.AppModule
import org.resolvetosavelives.red.di.DaggerTestAppComponent
import org.resolvetosavelives.red.di.TestAppComponent
import org.resolvetosavelives.red.sync.SyncConfig
import org.resolvetosavelives.red.sync.SyncModule
import org.resolvetosavelives.red.sync.SyncScheduler
import org.threeten.bp.Duration
import javax.inject.Inject

/**
 * This application class makes it possible to inject Android tests with their dependencies.
 * Using [appComponent] in a test's @Before function is a good place to start.
 */
class TestRedApp : RedApp() {

  @Inject
  lateinit var syncScheduler: SyncScheduler

  override fun onCreate() {
    super.onCreate()
    appComponent().inject(this)
    syncScheduler.cancelAll()
  }

  companion object {
    fun appComponent(): TestAppComponent {
      return RedApp.appComponent as TestAppComponent
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
