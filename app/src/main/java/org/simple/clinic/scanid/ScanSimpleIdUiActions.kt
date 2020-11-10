package org.simple.clinic.scanid

interface ScanSimpleIdUiActions {
  fun showQrCodeScannerView()
  fun hideQrCodeScannerView()
  fun hideShortCodeValidationError()
  fun showShortCodeValidationError(failure: ShortCodeValidationResult)
  fun sendScannedId(scanResult: ScanResult)
}
