package org.simple.clinic.qrscan

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import android.widget.FrameLayout
import github.nisrulz.qreader.QRDataListener
import github.nisrulz.qreader.QREader
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class GoogleVisionBasedQrCodeScanner(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), QrCodeScannerView {

  private var surfaceView: SurfaceView = SurfaceView(context)
  private val scannedQrCodes: Subject<String> = PublishSubject.create()
  private lateinit var qReader: QREader

  init {
    addView(surfaceView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val listener = QRDataListener { qrCode -> scannedQrCodes.onNext(qrCode) }

    qReader = QREader.Builder(context, surfaceView, listener)
        .facing(QREader.BACK_CAM)
        .enableAutofocus(true)
        .facing(QREader.BACK_CAM)
        .height(surfaceView.height)
        .width(surfaceView.width)
        .build()

    qReader.initAndStart(surfaceView)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    qReader.releaseAndCleanup()
  }

  override fun start() {
    // QREader throws an exception if the camera is already running.
    if (!qReader.isCameraRunning) {
      qReader.initAndStart(surfaceView)

      // Bug workaround: QREader waits for SurfaceView to perform a layout before
      // actually executing init() and start(). But SurfaceView doesn't seem to
      // give a callback if it has already initialized itself during inflation.
      // Performing a force re-layout does the trick.
      surfaceView.requestLayout()
    }
  }

  override fun stop() {
    if (qReader.isCameraRunning) {
      qReader.stop()
    }
  }

  override fun scans(): Observable<String> {
    return scannedQrCodes
  }
}
