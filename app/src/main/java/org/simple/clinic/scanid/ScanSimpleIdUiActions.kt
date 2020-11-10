package org.simple.clinic.scanid

import org.simple.clinic.patient.businessid.Identifier

interface ScanSimpleIdUiActions {
  fun showQrCodeScannerView()
  fun hideQrCodeScannerView()
  fun hideShortCodeValidationError()
  fun showShortCodeValidationError(failure: ShortCodeValidationResult)
  fun sendScannedId(identifier: Identifier)
}
