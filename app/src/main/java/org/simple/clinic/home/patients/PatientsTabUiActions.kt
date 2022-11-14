package org.simple.clinic.home.patients

import org.simple.clinic.appupdate.AppUpdateNudgePriority
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface PatientsTabUiActions {
  fun openEnterCodeManuallyScreen()
  fun openPatientSearchScreen(additionalIdentifier: Identifier?)
  fun showUserStatusAsApproved()
  fun hideUserAccountStatus()
  fun openScanSimpleIdCardScreen()
  fun showAppUpdateDialog()
  fun openPatientSummary(patientId: UUID)
  fun openSimpleOnPlaystore()
  fun showCriticalAppUpdateDialog(appUpdateNudgePriority: AppUpdateNudgePriority)
  fun openEnterDrugStockScreen()
  fun showNoActiveNetworkConnectionDialog()
  fun openPatientLineListDownloadDialog()
}
