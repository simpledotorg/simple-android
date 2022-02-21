package org.simple.clinic.appupdate

import android.app.Application
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import io.reactivex.Observable
import org.simple.clinic.BuildConfig
import org.simple.clinic.appupdate.AppUpdateState.AppUpdateStateError
import org.simple.clinic.appupdate.AppUpdateState.DontShowAppUpdate
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.feature.Feature.NotifyAppUpdateAvailable
import org.simple.clinic.feature.Features
import org.simple.clinic.settings.AppVersionFetcher
import javax.inject.Inject

class CheckAppUpdateAvailability @Inject constructor(
    private val appContext: Application,
    private val config: Observable<AppUpdateConfig>,
    private val updateManager: UpdateManager,
    private val versionUpdateCheck: (Int, Application, AppUpdateConfig) -> Boolean = isVersionApplicableForUpdate,
    private val features: Features,
    private val appVersionFetcher: AppVersionFetcher
) {

  fun listen(): Observable<AppUpdateState> {
    return updateManager
        .updateInfo()
        .flatMap(this::shouldNudgeForUpdate)
        .onErrorReturn(::AppUpdateStateError)
  }

  fun listenAllUpdates(): Observable<AppUpdateState> {
    return updateManager
        .updateInfo()
        .map {
          if (it.isUpdateAvailable) {
            ShowAppUpdate
          } else {
            DontShowAppUpdate
          }
        }
        .onErrorReturn(::AppUpdateStateError)
  }

  @VisibleForTesting(otherwise = PRIVATE)
  fun shouldNudgeForUpdate(updateInfo: UpdateInfo): Observable<AppUpdateState> {
    val checkUpdate = config
        .map { checkForUpdate(updateInfo, it) }

    val shouldShow = checkUpdate
        .filter { showUpdate -> showUpdate }
        .map { ShowAppUpdate }

    val doNotShow = checkUpdate
        .filter { showUpdate -> showUpdate.not() }
        .map { DontShowAppUpdate }

    return Observable.mergeArray(shouldShow, doNotShow)
  }

  private fun checkForUpdate(updateInfo: UpdateInfo, config: AppUpdateConfig): Boolean {
    return features.isEnabled(NotifyAppUpdateAvailable)
        && updateInfo.isUpdateAvailable
        && versionUpdateCheck(updateInfo.availableVersionCode, appContext, config)
  }
}

private val isVersionApplicableForUpdate = { availableVersionCode: Int, appContext: Application, config: AppUpdateConfig ->
  val packageInfo = appContext.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    val longVersionCode = packageInfo.longVersionCode
    val versionCode = longVersionCode.and(0xffffffff)
    availableVersionCode.minus(versionCode) >= config.differenceBetweenVersionsToNudge

  } else {
    availableVersionCode.minus(packageInfo.versionCode) >= config.differenceBetweenVersionsToNudge
  }
}
