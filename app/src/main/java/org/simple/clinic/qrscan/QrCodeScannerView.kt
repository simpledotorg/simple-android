package org.simple.clinic.qrscan

import io.reactivex.Observable

interface QrCodeScannerView {

  fun start()

  fun stop()

  fun scans(): Observable<String>
}
