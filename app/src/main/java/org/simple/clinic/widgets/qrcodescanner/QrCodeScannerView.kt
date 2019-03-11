package org.simple.clinic.widgets.qrcodescanner

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.BarcodeFormat
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import timber.log.Timber
import javax.inject.Inject

class QrCodeScannerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var lifecycle: Observable<TheActivityLifecycle>

  @Inject
  lateinit var controller: QrCodeScannerViewController

  private val scannerView = CodeScannerView(context)
      .apply {
        isAutoFocusButtonVisible = false
        isFlashButtonVisible = false
        maskColor = Color.TRANSPARENT
        // This sets the QR code scanning area to the full width of the screen
        frameSize = 1F
        frameColor = Color.TRANSPARENT
      }

  private val codeScanner by lazy(LazyThreadSafetyMode.NONE) { CodeScanner(context, scannerView) }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)
    addView(scannerView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    initializeCodeScanner()

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }

    Observable
        .merge(
            screenCreates(),
            screenDestroys,
            lifecycle)
        .compose(controller)
        .takeUntil(screenDestroys)
        .observeOn(mainThread())
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun initializeCodeScanner() {
    codeScanner.apply {
      camera = CodeScanner.CAMERA_BACK
      formats = listOf(BarcodeFormat.QR_CODE)
      autoFocusMode = AutoFocusMode.SAFE
      scanMode = ScanMode.CONTINUOUS
      isAutoFocusEnabled = true
      isFlashEnabled = false
      setAutoFocusInterval(1000L)

      errorCallback = ErrorCallback {
        // Intentionally pushing this because we don't know what are the errors that can
        // happen when trying to scan QR codes.
        Timber.e(it)
      }
    }
  }

  fun scans(): Observable<String> {
    return Observable.create { emitter ->
      codeScanner.decodeCallback = DecodeCallback { result ->
        if (result.barcodeFormat == BarcodeFormat.QR_CODE) {
          emitter.onNext(result.text)
        }
      }

      emitter.setCancellable {
        codeScanner.decodeCallback = null
      }
    }
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated())

  fun stopScanning() {
    codeScanner.releaseResources()
  }

  fun startScanning() {
    // If a keyboard is open on a screen later in the user flow, and hitting back on that screen
    // pops the navigation stack where we hide the keyboard, there is a weird bug where the camera
    // preview is sometimes distorted because the view size is reported incorrectly. This is a
    // "fix" (insert :sad-parrot gif) for this bug.
    postDelayed({ codeScanner.startPreview() }, 100L)
  }
}
