package org.simple.clinic

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.test.platform.app.InstrumentationRegistry
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.core.configuration.UploadFrequency
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.RumMonitor
import com.datadog.android.rum.tracking.ViewTrackingStrategy
import com.datadog.android.tracing.AndroidTracer
import com.tspoon.traceur.Traceur
import io.opentracing.util.GlobalTracer
import org.simple.clinic.TestClinicApp.Companion.appComponent
import org.simple.clinic.benchmark.BackupBenchmarkDatabase
import org.simple.clinic.di.DaggerTestAppComponent
import org.simple.clinic.di.TestAppComponent
import org.simple.clinic.di.TestAppModule
import org.simple.clinic.facility.FacilityPullResult
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.login.LoginResult
import org.simple.clinic.login.LoginUserWithOtp
import org.simple.clinic.login.activateuser.ActivateUser
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.finduser.FindUserResult
import org.simple.clinic.user.finduser.UserLookup
import org.simple.clinic.util.scheduler.SchedulersProvider
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

/**
 * This application class makes it possible to inject Android tests with their dependencies.
 * Using [appComponent] in a test's @Before function is a good place to start.
 */
class TestClinicApp : Application() {

  companion object {
    private lateinit var appComponent: TestAppComponent

    var isInBenchmarkMode = false

    fun appComponent(): TestAppComponent {
      return appComponent
    }
  }

  @Inject
  lateinit var activateUser: ActivateUser

  @Inject
  lateinit var loginUserWithOtp: LoginUserWithOtp

  @Inject
  lateinit var userLookup: UserLookup

  @Inject
  @Named("user_pin")
  lateinit var userPin: String

  @Inject
  @Named("user_phone_number")
  lateinit var userPhone: String

  @Inject
  @Named("user_otp")
  lateinit var userOtp: String

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var facilitySync: FacilitySync

  @Inject
  lateinit var dataSync: DataSync

  @Inject
  lateinit var backupBenchmarkDatabase: BackupBenchmarkDatabase

  override fun onCreate() {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
    Traceur.enableLogging()

    appComponent = buildDaggerGraph()
    appComponent.inject(this)

    ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
      override fun executeOnDiskIO(runnable: Runnable) {
        runnable.run()
      }

      override fun postToMainThread(runnable: Runnable) {
        runnable.run()
      }

      override fun isMainThread() = true
    })

    val instrumentationArgs = InstrumentationRegistry.getArguments()
    isInBenchmarkMode = instrumentationArgs.getString("benchmark_app_performance", "false").toBooleanStrict()
    if (isInBenchmarkMode) {
      loginWithBenchmarkUser()
      dataSync.syncTheWorld()
      backupBenchmarkDatabase.backup()

      setupDatadog(
          clientToken = instrumentationArgs.getString("dd_client_token")!!,
          applicationId = instrumentationArgs.getString("dd_application_id")!!
      )
    }
  }

  private fun loginWithBenchmarkUser() {
    // This is being run in an Rx chain because we cannot run blocking calls here because it is
    // run on the main thread and Android throws a crash
    userSession
        .logout()
        .subscribeOn(schedulersProvider.io())
        .map { facilitySync.pullWithResult() }
        .map { facilityPullResult ->
          if (facilityPullResult is FacilityPullResult.Success)
            facilityPullResult
          else
            throw RuntimeException("Could not fetch facilities: $facilityPullResult")
        }
        .map { userLookup.find(userPhone) }
        .map { findUserResult ->
          if (findUserResult is FindUserResult.Found)
            findUserResult
          else
            throw RuntimeException("Could not find user: $findUserResult")
        }
        .map { foundUser ->
          activateUser.activate(foundUser.uuid, userPin)
        }
        .map { activateUserResult ->
          if (activateUserResult is ActivateUser.Result.Success)
            activateUserResult
          else
            throw RuntimeException("Could not activate user: $activateUserResult")
        }
        .flatMap {
          loginUserWithOtp.loginWithOtp(userPhone, userPin, userOtp)
        }
        .map { loginUserResult ->
          if (loginUserResult is LoginResult.Success)
            loginUserResult
          else
            throw RuntimeException("Could not login user: $loginUserResult")
        }
        .ignoreElement()
        .blockingAwait()
  }

  private fun buildDaggerGraph(): TestAppComponent {
    return DaggerTestAppComponent.builder()
        .testAppModule(TestAppModule(this))
        .build()
  }

  private fun setupDatadog(
      clientToken: String,
      applicationId: String
  ) {
    val datadogConfig = Configuration
        .Builder(
            logsEnabled = true,
            tracesEnabled = true,
            crashReportsEnabled = false,
            rumEnabled = false
        )
        .useViewTrackingStrategy(NoopViewTrackingStrategy())
        .setUploadFrequency(UploadFrequency.FREQUENT)
        .build()
    val credentials = Credentials(
        clientToken = clientToken,
        envName = "test",
        variant = BuildConfig.FLAVOR,
        rumApplicationId = applicationId,
        serviceName = "simple-android-perf-regression"
    )
    Datadog.initialize(this, credentials, datadogConfig, TrackingConsent.GRANTED)
    GlobalRum.registerIfAbsent(RumMonitor.Builder().build())
    GlobalTracer.registerIfAbsent(AndroidTracer.Builder().setPartialFlushThreshold(5).build())
  }

  private class NoopViewTrackingStrategy : ViewTrackingStrategy {
    override fun register(context: Context) {
      // No need to track views in tests
    }

    override fun unregister(context: Context?) {
      // No need to track views in tests
    }
  }
}
