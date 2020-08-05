package org.simple.clinic.scanid

import java.util.UUID

interface ScanSimpleIdUiActions {
  fun showQrCodeScannerView()
  fun hideQrCodeScannerView()
  fun hideShortCodeValidationError()
  fun showShortCodeValidationError(failure: ShortCodeValidationResult)
  fun openPatientShortCodeSearch(validShortCode: String)
  fun openPatientSummary(patientUuid: UUID)
}
