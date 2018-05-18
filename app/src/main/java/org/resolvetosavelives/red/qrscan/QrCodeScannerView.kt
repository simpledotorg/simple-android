package org.resolvetosavelives.red.qrscan

import io.reactivex.Observable

interface QrCodeScannerView {

  fun setup()

  fun start()

  fun stop()

  fun scans(): Observable<String>
}
