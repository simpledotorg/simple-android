package org.simple.clinic.home.patients

interface PatientsUi: PatientsUiActions {
  fun showUserStatusAsPendingVerification()
  fun hideUserAccountStatus()
  fun openScanSimpleIdCardScreen()
  fun hideSyncIndicator()
  fun showSyncIndicator()
  fun showAppUpdateDialog()
  fun showSimpleVideo()
  fun showIllustration()
  fun openYouTubeLinkForSimpleVideo()
}
