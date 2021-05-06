package org.simple.clinic.scanid

interface ScanSimpleIdUiActions {
  fun showQrCodeScannerView()
  fun hideQrCodeScannerView()
  fun hideShortCodeValidationError()
  fun showShortCodeValidationError(failure: EnteredCodeValidationResult)
  fun sendScannedId(scanResult: ScanResult)
}
