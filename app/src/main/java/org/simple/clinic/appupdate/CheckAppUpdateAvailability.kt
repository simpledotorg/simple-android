package org.simple.clinic.appupdate

import android.app.Application
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.UpdateAvailability
import io.reactivex.Single
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toOptional
import javax.inject.Inject

class CheckAppUpdateAvailability @Inject constructor(
    private val appContext: Application,
    private val config: AppUpdateConfig,
    private val versionUpdateCheck: (Int, Application, AppUpdateConfig) -> Boolean = isVersionApplicableForUpdate
) {

  fun listen(): Single<Optional<AppUpdateData>> {
    val appUpdateManager = AppUpdateManagerFactory.create(appContext)
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo

    return Single.create { emitter ->

      appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
        if (shouldNudgeForAppUpdate(appUpdateInfo)) {
          emitter.onSuccess(AppUpdateData(appUpdateInfo).toOptional())
        } else {
          emitter.onSuccess(None)
        }
      }

      appUpdateInfoTask.addOnFailureListener { exception ->
        emitter.onError(exception)
      }
    }
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  fun shouldNudgeForAppUpdate(appUpdateInfo: AppUpdateInfo): Boolean {
    return config.inAppUpdateEnabled
        && appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        && appUpdateInfo.isUpdateTypeAllowed(FLEXIBLE)
        && versionUpdateCheck(appUpdateInfo.availableVersionCode(), appContext, config)
  }
}

private val isVersionApplicableForUpdate = { availableVersionCode: Int, appContext: Application, config: AppUpdateConfig ->
  val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    val longVersionCode = packageInfo.longVersionCode
    val versionCode = longVersionCode.and(0xffffffff)
    availableVersionCode.minus(versionCode) >= config.differenceBetweenVersionsToNudge

  } else {
    availableVersionCode.minus(packageInfo.versionCode) >= config.differenceBetweenVersionsToNudge
  }
}
