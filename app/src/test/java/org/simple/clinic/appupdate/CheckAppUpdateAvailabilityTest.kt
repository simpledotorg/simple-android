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
import org.simple.sharedTestCode.remoteconfig.DefaultValueConfigReader
import org.simple.sharedTestCode.remoteconfig.NoOpRemoteConfigService
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

  private val updateManager = mock<UpdateManager>()

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
        isUpdateAvailable = isUpdateAvailable
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
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = null, appStaleness = null)
        ),
        testCase(
            versionCode = 2111,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = null, appStaleness = null)
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
        isUpdateAvailable = isUpdateAvailable
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
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.LIGHT, appStaleness = 59),
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 215,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.CRITICAL, appStaleness = 214),
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 60,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.LIGHT, appStaleness = 59),
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 120,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.MEDIUM, appStaleness = 119),
            appUpdatePriority = 0
        ),
        testCase(
            versionCode = 125,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.CRITICAL_SECURITY, appStaleness = 124),
            appUpdatePriority = 5
        ),
        testCase(
            versionCode = 35,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            isInAppUpdateEnabled = true,
            appUpdateState = ShowAppUpdate(appUpdateNudgePriority = AppUpdateNudgePriority.LIGHT, appStaleness = 34),
            appUpdatePriority = 1
        )
    )
  }

  @Test
  fun `app staleness should be loaded correctly`() {
    // given
    setup(isInAppUpdateEnabled = false, isInAppUpdateEnabledV2 = false)
    val updateInfo = UpdateInfo(isUpdateAvailable = true, availableVersionCode = 150)
    whenever(updateManager.updateInfo()).doReturn(Observable.just(updateInfo))

    // then
    checkUpdateAvailable.loadAppStaleness().test().assertValue(149)
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

    val updatePriorities = mapOf(Pair("125", 5), Pair("35", 1))

    checkUpdateAvailable = CheckAppUpdateAvailability(
        config = config,
        updateManager = updateManager,
        versionUpdateCheck = versionCodeCheck,
        features = features,
        appVersionFetcher = appVersionFetcher,
        updatePriorities = updatePriorities
    )
  }
}
