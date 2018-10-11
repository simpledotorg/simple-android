package org.simple.clinic

import com.tspoon.traceur.Traceur
import io.reactivex.Single
import org.simple.clinic.TestClinicApp.Companion.appComponent
import org.simple.clinic.crash.CrashReporterModule
import org.simple.clinic.crash.NoOpCrashReporter
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.AppSqliteOpenHelperFactory
import org.simple.clinic.di.DaggerTestAppComponent
import org.simple.clinic.di.TestAppComponent
import org.simple.clinic.storage.StorageModule
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncModule
import org.simple.clinic.sync.SyncScheduler
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import timber.log.Timber
import javax.inject.Inject

/**
 * This application class makes it possible to inject Android tests with their dependencies.
 * Using [appComponent] in a test's @Before function is a good place to start.
 */
class TestClinicApp : ClinicApp() {

  @Inject
  lateinit var syncScheduler: SyncScheduler

  companion object {
    fun appComponent(): TestAppComponent {
      return ClinicApp.appComponent as TestAppComponent
    }
  }

  override fun onCreate() {
    super.onCreate()

    Timber.plant(Timber.DebugTree())
    Traceur.enableLogging()

    appComponent().inject(this)
    syncScheduler.cancelAll()
  }

  override fun buildDaggerGraph(): AppComponent {
    // We have moved the in-memory database configuration to the sqlite openhelper factory
    // but we still have to provide a non-empty name for Room, otherwise it complains.
    return DaggerTestAppComponent.builder()
        .appModule(object : AppModule(this, "ignored-db-name", runDatabaseQueriesOnMainThread = true) {
          override fun clock() = Clock.fixed(Instant.now(), ZoneOffset.UTC)
        })
        .storageModule(object : StorageModule() {
          override fun sqliteOpenHelperFactory() = AppSqliteOpenHelperFactory(inMemory = true)
        })
        .syncModule(object : SyncModule() {
          override fun syncConfig(): Single<SyncConfig> {
            return Single.just(SyncConfig(frequency = Duration.ofHours(1), batchSize = 10))
          }
        })
        .crashReporterModule(object : CrashReporterModule() {
          override fun crashReporter() = NoOpCrashReporter()
        })
        .build()
  }
}
