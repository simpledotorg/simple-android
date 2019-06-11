package org.simple.clinic.appupdate

import android.app.Application
import android.app.PendingIntent
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class CheckAppUpdateAvailabilityTest {

  private val config = mock<AppUpdateConfig>()

  lateinit var checkUpdateAvailable: CheckAppUpdateAvailability

  private val currentAppVersionCode = 1
  private val differenceInVersionsToShowUpdate = 1

  @Before
  fun setup() {

    val versionCodeCheck = { versionCode: Int, _: Application, _: AppUpdateConfig ->
      versionCode.minus(currentAppVersionCode) >= differenceInVersionsToShowUpdate
    }

    checkUpdateAvailable = CheckAppUpdateAvailability(mock(), config, versionCodeCheck)
  }

  @Test
  @Parameters(method = "params for checking app update")
  fun `when app update is available and it is eligible for updates, then the user should be nudged for an app update`(
      availableVersionCode: Int,
      updateAvailabilityState: Int,
      flexibleUpdateIntent: PendingIntent?,
      isInAppUpdateEnabled: Boolean,
      shouldShow: Boolean
  ) {
    val packageName = "org.simple.clinic"
    val immediateUpdateIntent = mock<PendingIntent>()

    val appUpdateInfo = AppUpdateInfo(
        packageName,
        availableVersionCode,
        updateAvailabilityState,
        InstallStatus.DOWNLOADED,
        immediateUpdateIntent,
        flexibleUpdateIntent
    )

    whenever(config.inAppUpdateEnabled).thenReturn(isInAppUpdateEnabled)
    whenever(config.differenceBetweenVersionsToNudge).thenReturn(1)

    val shouldShowDialog = checkUpdateAvailable.shouldNudgeForAppUpdate(appUpdateInfo)

    assertThat(shouldShowDialog).isEqualTo(shouldShow)
  }

  fun `params for checking app update`(): List<List<Any?>> {

    fun testCase(
        versionCode: Int,
        updateAvailabilityState: Int,
        flexibleUpdateIntent: PendingIntent?,
        isInAppUpdateEnabled: Boolean,
        shouldShow: Boolean
    ): List<Any?> {
      return listOf(versionCode, updateAvailabilityState, flexibleUpdateIntent, isInAppUpdateEnabled, shouldShow)
    }

    return listOf(
        testCase(
            versionCode = 1,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            flexibleUpdateIntent = mock(),
            isInAppUpdateEnabled = true,
            shouldShow = false
        ),
        testCase(
            versionCode = 2111,
            updateAvailabilityState = UpdateAvailability.UPDATE_NOT_AVAILABLE,
            flexibleUpdateIntent = mock(),
            isInAppUpdateEnabled = true,
            shouldShow = false
        ),
        testCase(
            versionCode = 2000,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            flexibleUpdateIntent = null,
            isInAppUpdateEnabled = true,
            shouldShow = false
        ),
        testCase(
            versionCode = 1000,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            flexibleUpdateIntent = mock(),
            isInAppUpdateEnabled = false,
            shouldShow = false
        ),
        testCase(
            versionCode = 2,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            flexibleUpdateIntent = mock(),
            isInAppUpdateEnabled = true,
            shouldShow = true
        ),
        testCase(
            versionCode = 2111,
            updateAvailabilityState = UpdateAvailability.UPDATE_AVAILABLE,
            flexibleUpdateIntent = mock(),
            isInAppUpdateEnabled = true,
            shouldShow = true
        )
    )
  }
}
