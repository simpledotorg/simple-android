package org.simple.clinic.appupdate

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import io.reactivex.Observable
import org.simple.clinic.CRITICAL_SECURITY_APP_UPDATE_PRIORITY
import org.simple.clinic.LIGHT_APP_UPDATE_PRIORITY
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL_SECURITY
import org.simple.clinic.appupdate.AppUpdateNudgePriority.LIGHT
import org.simple.clinic.appupdate.AppUpdateNudgePriority.MEDIUM
import org.simple.clinic.appupdate.AppUpdateState.AppUpdateStateError
import org.simple.clinic.appupdate.AppUpdateState.DontShowAppUpdate
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.feature.Feature.NotifyAppUpdateAvailable
import org.simple.clinic.feature.Feature.NotifyAppUpdateAvailableV2
import org.simple.clinic.feature.Features
import org.simple.clinic.settings.AppVersionFetcher
import org.simple.clinic.util.toOptional
import java.util.Optional
import javax.inject.Inject

class CheckAppUpdateAvailability @Inject constructor(
    private val config: Observable<AppUpdateConfig>,
    private val updateManager: UpdateManager,
    private val versionUpdateCheck: (Int, Int, AppUpdateConfig) -> Boolean = isVersionApplicableForUpdate,
    private val features: Features,
    private val appVersionFetcher: AppVersionFetcher
) {

  fun listen(): Observable<AppUpdateState> {
    return updateManager
        .updateInfo()
        .flatMap(this::shouldNudgeForUpdateBasedOnFeatureFlag)
        .onErrorReturn(::AppUpdateStateError)
  }

  private fun shouldNudgeForUpdateBasedOnFeatureFlag(updateInfo: UpdateInfo) =
      if (features.isEnabled(NotifyAppUpdateAvailableV2)) {
        shouldNudgeForUpdate(updateInfo)
      } else {
        shouldNudgeForUpdate_Old(updateInfo)
      }

  fun listenAllUpdates(): Observable<AppUpdateState> {
    return updateManager
        .updateInfo()
        .map {
          if (it.isUpdateAvailable) {
            ShowAppUpdate(appUpdateNudgePriority = null)
          } else {
            DontShowAppUpdate
          }
        }
        .onErrorReturn(::AppUpdateStateError)
  }

  @VisibleForTesting(otherwise = PRIVATE)
  fun shouldNudgeForUpdate(updateInfo: UpdateInfo): Observable<AppUpdateState> {
    val appStaleness = appVersionStaleness(updateInfo.availableVersionCode)
    val appUpdatePriority = config
        .map {
          appUpdateNudgePriority(
              appStaleness = appStaleness,
              config = it,
              appUpdatePriority = updateInfo.appUpdatePriority
          )
        }

    return appUpdatePriority
        .map { updatePriority ->
          val canShowAppUpdateNotification = updateInfo.isUpdateAvailable && updatePriority.isPresent
          if (features.isEnabled(NotifyAppUpdateAvailableV2) && canShowAppUpdateNotification) {
            ShowAppUpdate(updatePriority.get())
          } else {
            DontShowAppUpdate
          }
        }
  }

  @VisibleForTesting(otherwise = PRIVATE)
  fun shouldNudgeForUpdate_Old(updateInfo: UpdateInfo): Observable<AppUpdateState> {
    val checkUpdate = config
        .map { checkForUpdate(updateInfo, it) }

    val shouldShow = checkUpdate
        .filter { showUpdate -> showUpdate }
        .map { ShowAppUpdate(appUpdateNudgePriority = null) }

    val doNotShow = checkUpdate
        .filter { showUpdate -> showUpdate.not() }
        .map { DontShowAppUpdate }

    return Observable.mergeArray(shouldShow, doNotShow)
  }

  private fun checkForUpdate(updateInfo: UpdateInfo, config: AppUpdateConfig): Boolean {
    return features.isEnabled(NotifyAppUpdateAvailable)
        && updateInfo.isUpdateAvailable
        && versionUpdateCheck(updateInfo.availableVersionCode, appVersionFetcher.appVersionCode(), config)
  }

  private fun appUpdateNudgePriority(
      appStaleness: Int,
      config: AppUpdateConfig,
      appUpdatePriority: Int
  ): Optional<AppUpdateNudgePriority> =
      when {
        appUpdatePriority == CRITICAL_SECURITY_APP_UPDATE_PRIORITY -> CRITICAL_SECURITY
        appStaleness > config.differenceBetweenVersionsForCriticalNudge -> CRITICAL
        appStaleness > config.differenceBetweenVersionsForMediumNudge -> MEDIUM
        appStaleness >= config.differenceBetweenVersionsForLightNudge -> LIGHT
        appUpdatePriority == LIGHT_APP_UPDATE_PRIORITY -> LIGHT
        else -> null
      }.toOptional()

  private fun appVersionStaleness(availableVersionCode: Int) = availableVersionCode.minus(appVersionFetcher.appVersionCode())
}

private val isVersionApplicableForUpdate = { availableVersionCode: Int, appVersionCode: Int, config: AppUpdateConfig ->
  availableVersionCode.minus(appVersionCode) >= config.differenceBetweenVersionsToNudge
}
