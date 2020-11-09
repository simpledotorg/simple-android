package org.simple.clinic.scanid

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface ScanSimpleIdUiActions {
  fun showQrCodeScannerView()
  fun hideQrCodeScannerView()
  fun hideShortCodeValidationError()
  fun showShortCodeValidationError(failure: ShortCodeValidationResult)
  fun openPatientShortCodeSearch(validShortCode: String)
  fun openPatientSummary(patientUuid: UUID)
  fun openAddIdToPatientScreen(identifier: Identifier)
  fun sendScannedId(identifier: Identifier)
}
