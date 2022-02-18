package org.simple.clinic.appupdate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class AppUpdateState : Parcelable {

  @Parcelize
  data class ShowAppUpdate(val appUpdateNudgePriority: AppUpdateNudgePriority?) : AppUpdateState()

  @Parcelize
  data class AppUpdateStateError(val exception: Throwable) : AppUpdateState()

  @Parcelize
  object DontShowAppUpdate : AppUpdateState()
}
