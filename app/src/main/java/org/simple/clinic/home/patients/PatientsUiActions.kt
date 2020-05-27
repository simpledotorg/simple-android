package org.simple.clinic.home.patients

interface PatientsUiActions {
  fun openEnterCodeManuallyScreen()
  fun openPatientSearchScreen()
  fun showUserStatusAsWaiting()
  fun showUserStatusAsApproved()
  fun showUserStatusAsPendingVerification()
  fun hideUserAccountStatus()
  fun openScanSimpleIdCardScreen()
}
