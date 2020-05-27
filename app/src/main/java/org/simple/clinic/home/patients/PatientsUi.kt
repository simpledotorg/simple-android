package org.simple.clinic.home.patients

interface PatientsUi: PatientsUiActions {
  fun hideSyncIndicator()
  fun showSyncIndicator()
  fun showSimpleVideo()
  fun showIllustration()
}
