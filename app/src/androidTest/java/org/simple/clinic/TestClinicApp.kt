package org.simple.clinic

import com.tspoon.traceur.Traceur
import org.simple.clinic.TestClinicApp.Companion.appComponent
import org.simple.clinic.di.AppComponent
import org.simple.clinic.di.DaggerTestAppComponent
import org.simple.clinic.di.TestAppComponent
import org.simple.clinic.di.TestAppModule
import org.simple.clinic.di.TestCrashReporterModule
import org.simple.clinic.di.TestDataSyncOnApprovalModule
import org.simple.clinic.di.TestLoginModule
import org.simple.clinic.di.TestPatientModule
import org.simple.clinic.di.TestStorageModule
import org.simple.clinic.sync.SyncScheduler
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
      return appComponent as TestAppComponent
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
        .appModule(TestAppModule(this))
        .storageModule(TestStorageModule())
        .patientModule(TestPatientModule())
        .crashReporterModule(TestCrashReporterModule())
        .loginModule(TestLoginModule())
        .dataSyncOnApprovalModule(TestDataSyncOnApprovalModule())
        .build()
  }
}
