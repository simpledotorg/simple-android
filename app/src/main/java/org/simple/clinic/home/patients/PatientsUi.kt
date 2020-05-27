package org.simple.clinic.home.patients

interface PatientsUi: PatientsUiActions {
  fun hideSyncIndicator()
  fun showSyncIndicator()
  fun showAppUpdateDialog()
  fun showSimpleVideo()
  fun showIllustration()
  fun openYouTubeLinkForSimpleVideo()
}
