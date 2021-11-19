package org.simple.clinic

import android.app.Application
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.tspoon.traceur.Traceur
import org.simple.clinic.TestClinicApp.Companion.appComponent
import org.simple.clinic.di.DaggerTestAppComponent
import org.simple.clinic.di.TestAppComponent
import org.simple.clinic.di.TestAppModule
import timber.log.Timber

/**
 * This application class makes it possible to inject Android tests with their dependencies.
 * Using [appComponent] in a test's @Before function is a good place to start.
 */
class TestClinicApp : Application() {

  companion object {
    private lateinit var appComponent: TestAppComponent

    fun appComponent(): TestAppComponent {
      return appComponent
    }
  }

  override fun onCreate() {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
    Traceur.enableLogging()

    appComponent = buildDaggerGraph()
    ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
      override fun executeOnDiskIO(runnable: Runnable) {
        runnable.run()
      }

      override fun postToMainThread(runnable: Runnable) {
        runnable.run()
      }

      override fun isMainThread() = true
    })
  }

  private fun buildDaggerGraph(): TestAppComponent {
    return DaggerTestAppComponent.builder()
        .testAppModule(TestAppModule(this))
        .build()
  }
}
