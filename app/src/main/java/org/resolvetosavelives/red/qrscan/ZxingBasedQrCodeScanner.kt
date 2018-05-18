package org.resolvetosavelives.red.qrscan

import android.content.Context
import android.util.AttributeSet
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

class ZxingBasedQrCodeScanner(context: Context, attrs: AttributeSet) : QRCodeReaderView(context, attrs), QrCodeScannerView {

  override fun setup() {
    setQRDecodingEnabled(true)
    setAutofocusInterval(100L)
    setBackCamera()
  }

  override fun start() {
  }

  override fun stop() {
  }

  override fun scans(): Observable<String> {
    return Observable
        .create<String>({ emitter: ObservableEmitter<String> ->
          emitter.setCancellable({ setOnQRCodeReadListener(null) })
          setOnQRCodeReadListener({ text, _ -> emitter.onNext(text) })
        })
  }
}
