package org.simple.clinic.appupdate

sealed class AppUpdateState {

  object ShowAppUpdate : AppUpdateState()
  data class AppUpdateStateError(val exception: Throwable) : AppUpdateState()
  object DontShowAppUpdate : AppUpdateState()
}
