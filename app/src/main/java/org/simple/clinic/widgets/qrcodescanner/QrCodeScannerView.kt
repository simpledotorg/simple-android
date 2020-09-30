package org.simple.clinic.widgets.qrcodescanner

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.view_qrcode_scanner.view.*
import org.simple.clinic.R
import org.simple.clinic.activity.ActivityLifecycle
import org.simple.clinic.di.injector
import org.simple.clinic.main.TheActivity
import org.simple.clinic.widgets.ScreenDestroyed
import java.util.concurrent.Executors
import javax.inject.Inject

class QrCodeScannerView
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IQrCodeScannerView {

  @Inject
  lateinit var lifecycle: Observable<ActivityLifecycle>

  private val scans = PublishSubject.create<String>()

  private val qrCodeScannerLifecycle = QrCodeScannerLifecycle()
  private val cameraExecutor = Executors.newSingleThreadExecutor()

  private val cameraProviderFuture = ProcessCameraProvider.getInstance(context.applicationContext)

  init {
    View.inflate(context, R.layout.view_qrcode_scanner, this)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    context.injector<Injector>().inject(this)

    qrCodeScannerLifecycle.bindCamera()

    cameraProviderFuture.addListener(Runnable {
      val cameraProvider = cameraProviderFuture.get()
      startCamera(cameraProvider)
    }, ContextCompat.getMainExecutor(context))

    bindCameraToActivityLifecycle()
  }

  private fun startCamera(cameraProvider: ProcessCameraProvider) {
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    val preview = Preview.Builder().build()

    val analyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also {
          it.setAnalyzer(cameraExecutor, ZxingQrCodeAnalyzer(scans::onNext))
        }

    cameraProvider.unbindAll()

    cameraProvider.bindToLifecycle(qrCodeScannerLifecycle, cameraSelector, preview, analyzer)
    preview.setSurfaceProvider(previewView.createSurfaceProvider())
  }

  override fun onDetachedFromWindow() {
    cameraExecutor.shutdown()
    qrCodeScannerLifecycle.destroyCamera()
    super.onDetachedFromWindow()
  }

  override fun hideQrCodeScanner() {
    previewView.visibility = View.INVISIBLE
    viewFinderImageView.visibility = View.INVISIBLE
    qrCodeScannerLifecycle.unBindCamera()
  }

  override fun showQrCodeScanner() {
    previewView.visibility = View.VISIBLE
    viewFinderImageView.visibility = View.VISIBLE
    qrCodeScannerLifecycle.bindCamera()
  }

  override fun scans(): Observable<String> {
    return scans
  }

  private fun bindCameraToActivityLifecycle() {
    val screenDestroys = detaches()
        .map { ScreenDestroyed() }

    startScanningWhenActivityIsResumed(screenDestroys)
    stopScanningWhenActivityIsPaused(screenDestroys)
  }

  @SuppressLint("CheckResult")
  private fun startScanningWhenActivityIsResumed(screenDestroys: Observable<ScreenDestroyed>) {
    lifecycle
        .ofType<ActivityLifecycle.Resumed>()
        .takeUntil(screenDestroys)
        .subscribe { qrCodeScannerLifecycle.bindCamera() }
  }

  @SuppressLint("CheckResult")
  private fun stopScanningWhenActivityIsPaused(screenDestroys: Observable<ScreenDestroyed>) {
    lifecycle
        .ofType<ActivityLifecycle.Paused>()
        .takeUntil(screenDestroys)
        .subscribe { qrCodeScannerLifecycle.unBindCamera() }
  }

  interface Injector {
    fun inject(target: QrCodeScannerView)
  }
}
