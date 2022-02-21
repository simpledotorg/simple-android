package org.simple.clinic.appupdate

import android.app.Application
import com.google.android.play.core.install.model.UpdateAvailability
import com.nhaarman.mockitokotlin2.mock
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
  private val config = Observable.just(AppUpdateConfig(differenceBetweenVersionsToNudge = 1))

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
        isUpdateAvailable = isUpdateAvailable
    )

    setup(isFeatureEnabled = isInAppUpdateEnabled)

    val testObserver = checkUpdateAvailable
        .shouldNudgeForUpdate(updateInfo)
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
            appUpdateState = ShowAppUpdate
        ),
        testCase(
            versionCode = 2111,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
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
        config = config,
        updateManager = mock(),
        versionUpdateCheck = versionCodeCheck,
        features = features,
        appVersionFetcher = appVersionFetcher
    )
  }
}
