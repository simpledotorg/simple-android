package org.simple.clinic.scanid

import org.simple.clinic.patient.PatientPrefillInfo
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface ScanSimpleIdUiActions {
  fun showQrCodeScannerView()
  fun hideQrCodeScannerView()
  fun hideEnteredCodeValidationError()
  fun showEnteredCodeValidationError(failure: EnteredCodeValidationResult)
  fun openPatientSummary(patientId: UUID)
  fun openPatientSearch(additionalIdentifier: Identifier?, initialSearchQuery: String?, patientPrefillInfo: PatientPrefillInfo?)
  fun goBackToEditPatientScreen(identifier: Identifier)
  fun showPatientWithIdentifierExistsError()
  fun showInvalidQrCodeError()
}
