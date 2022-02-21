package org.simple.clinic.appupdate

import com.google.android.play.core.install.model.UpdateAvailability
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
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
import org.simple.clinic.settings.AppVersionFetcher

@RunWith(JUnitParamsRunner::class)
class CheckAppUpdateAvailabilityTest {

  private val currentAppVersionCode = 1
  private val differenceInVersionsToShowUpdate = 1
  private val config = Observable.just(
          AppUpdateConfig(
              differenceBetweenVersionsToNudge = 1,
              differenceBetweenVersionsForLightNudge = 30,
              differenceBetweenVersionsForMediumNudge = 61,
              differenceBetweenVersionsForCriticalNudge = 181
          ))

  private val appVersionFetcher = mock<AppVersionFetcher>()

  lateinit var checkUpdateAvailable: CheckAppUpdateAvailability

  @Test
  @Parameters(method = "params for checking app update")
  fun `when app update is available and it is eligible for updates, then the user should be nudged for an app update`(
      availableVersionCode: Int,
      updateAvailabilityState: Int,
      isInAppUpdateEnabled: Boolean,
      appUpdateState: AppUpdateState
  ) {
    val isUpdateAvailable = updateAvailabilityState == UpdateAvailability.UPDATE_AVAILABLE
    val updateInfo = UpdateInfo(
        availableVersionCode = availableVersionCode,
        isUpdateAvailable = isUpdateAvailable,
        appUpdatePriority = 0
    )

    setup(isInAppUpdateEnabled = isInAppUpdateEnabled, isInAppUpdateEnabledV2 = false)

    val testObserver = checkUpdateAvailable
        .shouldNudgeForUpdate_Old(updateInfo)
        .test()

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
        isInAppUpdateEnabled: Boolean,
        appUpdateState: AppUpdateState
    ): List<Any?> {
      return listOf(versionCode, updateAvailabilityState, isInAppUpdateEnabled, appUpdateState)
    }

    return listOf(
        testCase(
            versionCode = 1,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = DontShowAppUpdate
        ),
        testCase(
            versionCode = 2111,
            updateAvailabilityState = UpdateAvailability.UPDATE_NOT_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = DontShowAppUpdate
        ),
        testCase(
            versionCode = 1000,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = false,
            appUpdateState = DontShowAppUpdate
        ),
        testCase(
            versionCode = 2,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = null)
        ),
        testCase(
            versionCode = 2111,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = null)
        )
    )
  }

  @Test
  @Parameters(method = "params for checking app update v2")
  fun `when app update is available and app update v2 is enabled, then the user should be nudged for an app update with the nudge priority`(
      availableVersionCode: Int,
      updateAvailabilityState: Int,
      isInAppUpdateEnabled: Boolean,
      appUpdateState: AppUpdateState,
      appUpdatePriority: Int
  ) {
    val isUpdateAvailable = updateAvailabilityState == UpdateAvailability.UPDATE_AVAILABLE
    val updateInfo = UpdateInfo(
        availableVersionCode = availableVersionCode,
        isUpdateAvailable = isUpdateAvailable,
        appUpdatePriority = appUpdatePriority
    )

    setup(isInAppUpdateEnabled = false, isInAppUpdateEnabledV2 = isInAppUpdateEnabled)

    val testObserver = checkUpdateAvailable
        .shouldNudgeForUpdate(updateInfo)
        .test()

    with(testObserver) {
      assertNoErrors()
      assertSubscribed()
      assertValue(appUpdateState)
    }
  }

  fun `params for checking app update v2`(): List<List<Any?>> {

    fun testCase(
        versionCode: Int,
        updateAvailabilityState: Int,
        isInAppUpdateEnabled: Boolean,
        appUpdateState: AppUpdateState,
        appUpdatePriority: Int
    ): List<Any?> {
      return listOf(versionCode, updateAvailabilityState, isInAppUpdateEnabled, appUpdateState, appUpdatePriority)
    }

    return listOf(
        testCase(
            versionCode = 1,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = false,
            appUpdateState = DontShowAppUpdate,
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 2111,
            updateAvailabilityState = UpdateAvailability.UPDATE_NOT_AVAILABLE,
            isInAppUpdateEnabled = false,
            appUpdateState = DontShowAppUpdate,
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 1000,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = false,
            appUpdateState = DontShowAppUpdate,
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 2,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = false,
            appUpdateState = DontShowAppUpdate,
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 60,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.LIGHT),
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 215,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.CRITICAL),
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 60,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.LIGHT),
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 120,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.MEDIUM),
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 120,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.CRITICAL_SECURITY),
            appUpdatePriority = 5
        ),
        testCase(
            versionCode = 33,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.LIGHT),
            appUpdatePriority = 1
        )
    )
  }

  private fun setup(
      isInAppUpdateEnabled: Boolean,
      isInAppUpdateEnabledV2: Boolean
  ) {
    val versionCodeCheck = { versionCode: Int, _: Int, _: AppUpdateConfig ->
      versionCode.minus(currentAppVersionCode) >= differenceInVersionsToShowUpdate
    }

    val appVersionFetcher = mock<AppVersionFetcher>()

    whenever(appVersionFetcher.appVersionCode()) doReturn currentAppVersionCode

    val features = Features(
        remoteConfigService = NoOpRemoteConfigService(DefaultValueConfigReader()),
        overrides = mapOf(
            Feature.NotifyAppUpdateAvailable to isInAppUpdateEnabled,
            Feature.NotifyAppUpdateAvailableV2 to isInAppUpdateEnabledV2
        )
    )
    checkUpdateAvailable = CheckAppUpdateAvailability(
        config = config,
        updateManager = mock(),
        versionUpdateCheck = versionCodeCheck,
        features = features,
        appVersionFetcher = appVersionFetcher
    )
  }
}
