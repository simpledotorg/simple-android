package org.simple.clinic.home.patients

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface PatientsTabUiActions {
  fun openEnterCodeManuallyScreen()
  fun openPatientSearchScreen(additionalIdentifier: Identifier?)
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
