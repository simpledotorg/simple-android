package org.simple.clinic.widgets.qrcodescanner

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.databinding.ViewQrcodeScannerBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class QrCodeScannerView
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IQrCodeScannerView {

  private var binding: ViewQrcodeScannerBinding? = null

  private val previewView
    get() = binding!!.previewView

  private val viewFinderImageView
    get() = binding!!.viewFinderImageView

  companion object {
    private const val RATIO_4_3_VALUE = 4.0 / 3.0
    private const val RATIO_16_9_VALUE = 16.0 / 9.0
  }

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var bitmapUtils: BitmapUtils

  private val scans = PublishSubject.create<String>()

  private val cameraExecutor = Executors.newSingleThreadExecutor()

  private val cameraProviderFuture = ProcessCameraProvider.getInstance(context.applicationContext)

  init {
    val layoutInflater = LayoutInflater.from(context)
    binding = ViewQrcodeScannerBinding.inflate(layoutInflater, this, true)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    context.injector<Injector>().inject(this)

    cameraProviderFuture.addListener(Runnable {
      val cameraProvider = cameraProviderFuture.get()
      startCamera(cameraProvider)
    }, ContextCompat.getMainExecutor(context))
  }

  private fun startCamera(cameraProvider: ProcessCameraProvider) {
    // Get screen metrics used to setup camera for full screen resolution
    val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }

    val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

    val rotation = previewView.display.rotation

    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    val preview = Preview.Builder().build()

    val isMLKitQrCodeScannerEnabled = features.isEnabled(Feature.MLKitQrCodeScanner)
    val qrCodeAnalyzer = if (isMLKitQrCodeScannerEnabled) {
      MLKitQrCodeAnalyzer(bitmapUtils, scans::onNext)
    } else {
      ZxingQrCodeAnalyzer(scans::onNext)
    }

    val analyzer = ImageAnalysis.Builder()
        .setTargetAspectRatio(screenAspectRatio)
        .setTargetRotation(rotation)
        .build()
        .also {
          it.setAnalyzer(cameraExecutor, qrCodeAnalyzer)
        }

    cameraProvider.unbindAll()

    cameraProvider.bindToLifecycle(activity, cameraSelector, preview, analyzer)
    preview.setSurfaceProvider(previewView.surfaceProvider)
  }

  /**
   *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
   *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
   *
   *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
   *  of preview ratio to one of the provided values.
   *
   *  @param width - preview width
   *  @param height - preview height
   *  @return suitable aspect ratio
   *
   *  source: https://github.com/android/camera-samples/blob/bfc46b68ef51f337104d3487c2394b204cf0d453/CameraXBasic/app/src/main/java/com/android/example/cameraxbasic/fragments/CameraFragment.kt#L350
   */
  private fun aspectRatio(width: Int, height: Int): Int {
    val previewRatio = max(width, height).toDouble() / min(width, height)
    if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
      return AspectRatio.RATIO_4_3
    }
    return AspectRatio.RATIO_16_9
  }

  override fun onDetachedFromWindow() {
    cameraExecutor.shutdown()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun hideQrCodeScanner() {
    previewView.visibility = View.INVISIBLE
    viewFinderImageView.visibility = View.INVISIBLE
  }

  override fun showQrCodeScanner() {
    previewView.visibility = View.VISIBLE
    viewFinderImageView.visibility = View.VISIBLE
  }

  override fun scans(): Observable<String> {
    return scans
  }

  interface Injector {
    fun inject(target: QrCodeScannerView)
  }
}
