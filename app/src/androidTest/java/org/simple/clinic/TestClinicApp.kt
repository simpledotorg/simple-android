package org.simple.clinic

import android.app.Application
import com.tspoon.traceur.Traceur
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.OkHttpClient
import org.simple.clinic.TestClinicApp.Companion.appComponent
import org.simple.clinic.crash.CrashReporterModule
import org.simple.clinic.crash.NoOpCrashReporter
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.AppModule
import org.simple.clinic.di.AppSqliteOpenHelperFactory
import org.simple.clinic.di.DaggerTestAppComponent
import org.simple.clinic.di.NetworkModule
import org.simple.clinic.di.TestAppComponent
import org.simple.clinic.login.LoginModule
import org.simple.clinic.login.LoginOtpSmsListener
import org.simple.clinic.network.FailAllNetworkCallsInterceptor
import org.simple.clinic.patient.fuzzy.AbsoluteFuzzer
import org.simple.clinic.patient.fuzzy.AgeFuzzerModule
import org.simple.clinic.storage.StorageModule
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncModule
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.user.LoggedInUserHttpInterceptor
import org.simple.clinic.util.TestClock
import org.threeten.bp.Clock
import org.threeten.bp.Duration
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
          override fun clock(): Clock = TestClock()
        })
        .storageModule(object : StorageModule() {
          override fun sqliteOpenHelperFactory() = AppSqliteOpenHelperFactory(inMemory = true)
        })
        .syncModule(object : SyncModule() {
          override fun syncConfig(): Single<SyncConfig> {
            return Single.just(
                SyncConfig(
                    frequency = Duration.ofMinutes(16),
                    backOffDelay = Duration.ofMinutes(5),
                    batchSize = 10))
          }
        })
        .ageFuzzerModule(object : AgeFuzzerModule() {
          override fun provideAgeFuzzer() = AbsoluteFuzzer(5)
        })
        .crashReporterModule(object : CrashReporterModule() {
          override fun crashReporter() = NoOpCrashReporter()
        })
        .loginModule(object : LoginModule() {
          override fun loginSmsListener(app: Application): LoginOtpSmsListener {
            return object : LoginOtpSmsListener {
              override fun listenForLoginOtp(): Completable = Completable.complete()
            }
          }
        })
        .networkModule(object : NetworkModule() {
          override fun okHttpClient(loggedInInterceptor: LoggedInUserHttpInterceptor): OkHttpClient {
            return super.okHttpClient(loggedInInterceptor)
                .newBuilder()
                .addInterceptor(FailAllNetworkCallsInterceptor)
                .build()
          }
        })
        .build()
  }
}
