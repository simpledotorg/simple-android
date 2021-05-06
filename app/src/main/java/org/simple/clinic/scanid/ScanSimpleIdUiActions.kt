package org.simple.clinic.scanid

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface ScanSimpleIdUiActions {
  fun showQrCodeScannerView()
  fun hideQrCodeScannerView()
  fun hideShortCodeValidationError()
  fun showShortCodeValidationError(failure: EnteredCodeValidationResult)
  fun sendScannedId(scanResult: ScanResult)
  fun openPatientSummary(patientId: UUID)
  fun openShortCodeSearch(shortCode: String)
  fun openPatientSearch(additionalIdentifier: Identifier?)
}
