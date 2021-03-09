package org.simple.clinic.appupdate

import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import io.reactivex.Observable
import javax.inject.Inject

interface UpdateManager {
  fun updateInfo(): Observable<UpdateInfo>
}

class PlayUpdateManager @Inject constructor(
    private val appUpdateManager: AppUpdateManager
) : UpdateManager {

  override fun updateInfo(): Observable<UpdateInfo> {
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo
    return Observable.create { emitter ->
      var cancelled = false

      appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
        emitter.onNext(createUpdateInfo(appUpdateInfo))
      }

      appUpdateInfoTask.addOnFailureListener { exception ->
        if (cancelled.not()) emitter.onError(exception)
      }

      emitter.setCancellable { cancelled = true }
    }
  }

  private fun createUpdateInfo(appUpdateInfo: AppUpdateInfo) = UpdateInfo(
      availableVersionCode = appUpdateInfo.availableVersionCode(),
      isUpdateAvailable = appUpdateInfo.updateAvailability() == UPDATE_AVAILABLE,
      isFlexibleUpdateType = appUpdateInfo.isUpdateTypeAllowed(FLEXIBLE)
  )
}
