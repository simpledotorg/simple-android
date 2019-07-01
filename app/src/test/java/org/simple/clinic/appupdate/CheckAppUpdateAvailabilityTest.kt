package org.simple.clinic.appupdate

import android.app.Application
import android.app.PendingIntent
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.appupdate.AppUpdateState.*

@RunWith(JUnitParamsRunner::class)
class CheckAppUpdateAvailabilityTest {

  private val configProvider = PublishSubject.create<AppUpdateConfig>()
  private val currentAppVersionCode = 1
  private val differenceInVersionsToShowUpdate = 1

  lateinit var checkUpdateAvailable: CheckAppUpdateAvailability

  @Before
  fun setup() {

    val versionCodeCheck = { versionCode: Int, _: Application, _: AppUpdateConfig ->
      versionCode.minus(currentAppVersionCode) >= differenceInVersionsToShowUpdate
    }

    checkUpdateAvailable = CheckAppUpdateAvailability(mock(), configProvider, versionCodeCheck)
  }

  @Test
  @Parameters(method = "params for checking app update")
  fun `when app update is available and it is eligible for updates, then the user should be nudged for an app update`(
      availableVersionCode: Int,
      updateAvailabilityState: Int,
      flexibleUpdateIntent: PendingIntent?,
      isInAppUpdateEnabled: Boolean,
      appUpdateState: AppUpdateState
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

    val testObserver = checkUpdateAvailable
        .shouldNudgeForUpdate(appUpdateInfo)
        .test()

    configProvider.onNext(AppUpdateConfig(isInAppUpdateEnabled, 1))

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
}
