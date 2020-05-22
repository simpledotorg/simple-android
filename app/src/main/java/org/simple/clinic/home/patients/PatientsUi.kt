package org.simple.clinic.home.patients

interface PatientsUi {
  fun openPatientSearchScreen()
  fun showUserStatusAsWaiting()
  fun showUserStatusAsApproved()
  fun showUserStatusAsPendingVerification()
  fun hideUserAccountStatus()
  fun openEnterCodeManuallyScreen()
  fun openScanSimpleIdCardScreen()
  fun hideSyncIndicator()
  fun showSyncIndicator()
  fun showAppUpdateDialog()
  fun showSimpleVideo()
  fun showIllustration()
  fun openYouTubeLinkForSimpleVideo()
}
