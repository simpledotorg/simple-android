package org.simple.clinic

import com.tspoon.traceur.Traceur
import io.reactivex.Single
import org.simple.clinic.TestClinicApp.Companion.appComponent
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.AppSqliteOpenHelperFactory
import org.simple.clinic.di.DaggerTestAppComponent
import org.simple.clinic.di.StorageModule
import org.simple.clinic.di.TestAppComponent
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncModule
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.user.OngoingLoginEntry
import org.threeten.bp.Duration
import timber.log.Timber
import java.util.UUID
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

    fun qaUserUuid(): UUID {
      return UUID.fromString("c6834f82-3305-4144-9dc8-5f77c908ebf1")
    }

    fun qaOngoingLoginEntry(): OngoingLoginEntry {
      return OngoingLoginEntry(qaUserUuid(), phoneNumber = "0000", pin = "0000")
    }

    @Deprecated(message = "Get real facilities from the server instead. Look at UserSessionAndroidTest for examples.")
    fun qaUserFacilityUuid(): UUID {
      return UUID.fromString("43dad34c-139e-4e5f-976e-a3ef1d9ac977")
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
        .appModule(AppModule(this, "ignored-db-name"))
        .storageModule(object : StorageModule() {
          override fun sqliteOpenHelperFactory() = AppSqliteOpenHelperFactory(inMemory = true)
        })
        .syncModule(object : SyncModule() {
          override fun syncConfig(): Single<SyncConfig> {
            return Single.just(SyncConfig(frequency = Duration.ofHours(1), batchSize = 10))
          }
        })
        .build()
  }
}
