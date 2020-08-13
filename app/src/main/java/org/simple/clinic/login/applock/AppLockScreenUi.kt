package org.simple.clinic.login.applock

interface AppLockScreenUi : AppLockUiActions {
  fun setUserFullName(fullName: String)
  fun setFacilityName(facilityName: String)
  fun restorePreviousScreen()
  fun showConfirmResetPinDialog()
}
