package org.simple.clinic.appupdate

import android.app.Application
import android.app.PendingIntent
import com.google.android.play.core.install.model.UpdateAvailability
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.appupdate.AppUpdateState.DontShowAppUpdate
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.remoteconfig.DefaultValueConfigReader
import org.simple.clinic.remoteconfig.NoOpRemoteConfigService

@RunWith(JUnitParamsRunner::class)
class CheckAppUpdateAvailabilityTest {

  private val configProvider = PublishSubject.create<AppUpdateConfig>()
  private val currentAppVersionCode = 1
  private val differenceInVersionsToShowUpdate = 1

  lateinit var checkUpdateAvailable: CheckAppUpdateAvailability

  @Test
  @Parameters(method = "params for checking app update")
  fun `when app update is available and it is eligible for updates, then the user should be nudged for an app update`(
      availableVersionCode: Int,
      updateAvailabilityState: Int,
      flexibleUpdateIntent: PendingIntent?,
      isInAppUpdateEnabled: Boolean,
      appUpdateState: AppUpdateState
  ) {

    val isUpdateAvailable = updateAvailabilityState == UpdateAvailability.UPDATE_AVAILABLE
    val updateInfo = UpdateInfo(
        availableVersionCode = availableVersionCode,
        isUpdateAvailable = isUpdateAvailable,
        isFlexibleUpdateType = flexibleUpdateIntent != null
    )

    setup(isFeatureEnabled = isInAppUpdateEnabled)

    val testObserver = checkUpdateAvailable
        .shouldNudgeForUpdate(updateInfo)
        .test()

    configProvider.onNext(AppUpdateConfig(1))

    with(testObserver) {
      assertNoErrors()
      assertSubscribed()
      assertValue(appUpdateState)
    }
  }

  fun `params for checking app update`(): List<List<Any?>> {

    fun testCase(
        versionCode: Int,
        updateAvailabilityState: Int,
        flexibleUpdateIntent: PendingIntent?,
        isInAppUpdateEnabled: Boolean,
        appUpdateState: AppUpdateState
    ): List<Any?> {
      return listOf(versionCode, updateAvailabilityState, flexibleUpdateIntent, isInAppUpdateEnabled, appUpdateState)
    }

    return listOf(
        testCase(
            versionCode = 1,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            flexibleUpdateIntent = mock(),
            isInAppUpdateEnabled = true,
            appUpdateState = DontShowAppUpdate
        ),
        testCase(
            versionCode = 2111,
            updateAvailabilityState = UpdateAvailability.UPDATE_NOT_AVAILABLE,
            flexibleUpdateIntent = mock(),
            isInAppUpdateEnabled = true,
            appUpdateState = DontShowAppUpdate
        ),
        testCase(
            versionCode = 2000,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            flexibleUpdateIntent = null,
            isInAppUpdateEnabled = true,
            appUpdateState = DontShowAppUpdate
        ),
        testCase(
            versionCode = 1000,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            flexibleUpdateIntent = mock(),
            isInAppUpdateEnabled = false,
            appUpdateState = DontShowAppUpdate
        ),
        testCase(
            versionCode = 2,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            flexibleUpdateIntent = mock(),
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate
        ),
        testCase(
            versionCode = 2111,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            flexibleUpdateIntent = mock(),
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate
        )
    )
  }

  private fun setup(
      isFeatureEnabled: Boolean
  ) {
    val versionCodeCheck = { versionCode: Int, _: Application, _: AppUpdateConfig ->
      versionCode.minus(currentAppVersionCode) >= differenceInVersionsToShowUpdate
    }

    val features = Features(
        remoteConfigService = NoOpRemoteConfigService(DefaultValueConfigReader()),
        overrides = mapOf(Feature.NotifyAppUpdateAvailable to isFeatureEnabled)
    )
    checkUpdateAvailable = CheckAppUpdateAvailability(
        appContext = mock(),
        config = configProvider,
        updateManager = mock(),
        versionUpdateCheck = versionCodeCheck,
        features = features
    )
  }
}
