package org.simple.clinic.scanid

interface ScanSimpleIdUiActions {
  fun showQrCodeScannerView()
  fun hideQrCodeScannerView()
  fun hideEnteredCodeValidationError()
  fun showShortCodeValidationError(failure: EnteredCodeValidationResult)
  fun sendScannedId(scanResult: ScanResult)
}
