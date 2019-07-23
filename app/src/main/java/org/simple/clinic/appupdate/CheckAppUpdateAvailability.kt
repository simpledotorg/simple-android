package org.simple.clinic.appupdate

import android.app.Application
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.UpdateAvailability
import io.reactivex.Observable
import org.simple.clinic.BuildConfig
import org.simple.clinic.appupdate.AppUpdateState.AppUpdateStateError
import org.simple.clinic.appupdate.AppUpdateState.DontShowAppUpdate
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import javax.inject.Inject

class CheckAppUpdateAvailability @Inject constructor(
    private val appContext: Application,
    private val config: Observable<AppUpdateConfig>,
    private val versionUpdateCheck: (Int, Application, AppUpdateConfig) -> Boolean = isVersionApplicableForUpdate
) {

  fun listen(): Observable<AppUpdateState> {
    return appUpdateCallback()
        .flatMap(this::shouldNudgeForUpdate)
        .onErrorReturn(::AppUpdateStateError)
  }

  private fun appUpdateCallback(): Observable<AppUpdateInfo> {
    val appUpdateManager = AppUpdateManagerFactory.create(appContext)
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo

    return Observable.create<AppUpdateInfo> { emitter ->
      var cancelled = false

      appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
        emitter.onNext(appUpdateInfo)
      }

      appUpdateInfoTask.addOnFailureListener { exception ->
        if (cancelled.not()) {
          emitter.onError(exception)
        }
      }

      emitter.setCancellable { cancelled = true }
    }
  }

  @VisibleForTesting(otherwise = PRIVATE)
  fun shouldNudgeForUpdate(appUpdateInfo: AppUpdateInfo): Observable<AppUpdateState> {
    val checkUpdate = config
        .map { checkForUpdate(appUpdateInfo, it) }

    val shouldShow = checkUpdate
        .filter { showUpdate -> showUpdate }
        .map { ShowAppUpdate }

    val doNotShow = checkUpdate
        .filter { showUpdate -> showUpdate.not() }
        .map { DontShowAppUpdate }

    return Observable.mergeArray(shouldShow, doNotShow)
  }

  private fun checkForUpdate(appUpdateInfo: AppUpdateInfo, config: AppUpdateConfig): Boolean {
    return config.inAppUpdateEnabled
        && appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        && appUpdateInfo.isUpdateTypeAllowed(FLEXIBLE)
        && versionUpdateCheck(appUpdateInfo.availableVersionCode(), appContext, config)
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
