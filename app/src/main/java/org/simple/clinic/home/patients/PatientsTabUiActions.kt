package org.simple.clinic.home.patients

interface PatientsTabUiActions {
  fun openEnterCodeManuallyScreen()
  fun openPatientSearchScreen()
  fun showUserStatusAsWaiting()
  fun showUserStatusAsApproved()
  fun showUserStatusAsPendingVerification()
  fun hideUserAccountStatus()
  fun openScanSimpleIdCardScreen()
  fun openYouTubeLinkForSimpleVideo()
  fun showAppUpdateDialog()
  fun openShortCodeSearchScreen(shortCode: String)
}
