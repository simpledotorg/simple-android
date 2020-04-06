package org.simple.clinic.widgets.qrcodescanner

import io.reactivex.Observable

interface IQrCodeScannerView {
  fun hideQrCodeScanner()
  fun showQrCodeScanner()
  fun scans(): Observable<String>
}