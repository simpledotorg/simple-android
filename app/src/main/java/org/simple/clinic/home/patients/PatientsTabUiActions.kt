package org.simple.clinic.home.patients

import java.util.UUID

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
  fun openPatientSummary(patientId: UUID)
}
