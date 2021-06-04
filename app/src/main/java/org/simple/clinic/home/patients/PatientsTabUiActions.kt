package org.simple.clinic.home.patients

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface PatientsTabUiActions {
  fun openEnterCodeManuallyScreen()
  fun openPatientSearchScreen(additionalIdentifier: Identifier?)
  fun showUserStatusAsApproved()
  fun hideUserAccountStatus()
  fun openScanSimpleIdCardScreen()
  fun openYouTubeLinkForSimpleVideo()
  fun showAppUpdateDialog()
  fun openPatientSummary(patientId: UUID)
}
