package org.simple.clinic.login.applock

interface AppLockScreenUi {
  fun setUserFullName(fullName: String)
  fun setFacilityName(facilityName: String)
  fun restorePreviousScreen()
  fun exitApp()
  fun showConfirmResetPinDialog()
}
