package org.simple.clinic

import android.app.Application
import android.arch.persistence.room.Room
import com.tspoon.traceur.Traceur
import io.reactivex.Single
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import org.simple.clinic.TestClinicApp.Companion.appComponent
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.DaggerTestAppComponent
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

    fun qaOngoingLoginEntry(): OngoingLoginEntry {
      return OngoingLoginEntry("0000", "0000", "0000")
    }

    fun qaUserFacilityUUID(): UUID {
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
    return DaggerTestAppComponent.builder()
        .appModule(object : AppModule(this) {
          override fun appDatabase(appContext: Application): AppDatabase {
            return Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java)
                .openHelperFactory(RequerySQLiteOpenHelperFactory())
                .build()
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
