package org.simple.clinic.scanid

import java.util.UUID

interface ScanSimpleIdUiActions {
  fun showQrCodeScannerView()
  fun hideQrCodeScannerView()
  fun hideShortCodeValidationError()
  fun showShortCodeValidationError(failure: EnteredCodeValidationResult)
  fun sendScannedId(scanResult: ScanResult)
  fun openPatientSummary(patientId: UUID)
  fun openShortCodeSearch(shortCode: String)
}
