package org.simple.clinic.widgets.qrcodescanner

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.view_qrcode_scanner.view.*
import org.simple.clinic.R

class QrCodeScannerView @JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  private val qrCodeScannerLifecycle = QrCodeScannerLifecycle()

  private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

  init {
    View.inflate(context, R.layout.view_qrcode_scanner, this)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    qrCodeScannerLifecycle.bindCamera()

    cameraProviderFuture.addListener(Runnable {
      val cameraProvider = cameraProviderFuture.get()
      startCamera(cameraProvider)
    }, ContextCompat.getMainExecutor(context))
  }

  private fun startCamera(cameraProvider: ProcessCameraProvider) {
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    val preview = Preview.Builder().build()

    cameraProvider.unbindAll()

    val camera = cameraProvider.bindToLifecycle(qrCodeScannerLifecycle, cameraSelector, preview)
    preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.cameraInfo))
  }

  override fun onDetachedFromWindow() {
    qrCodeScannerLifecycle.destroyCamera()
    super.onDetachedFromWindow()
  }

  fun hideQrCodeScanner() {
    previewView.visibility = View.INVISIBLE
    viewFinderImageView.visibility = View.INVISIBLE
    qrCodeScannerLifecycle.unBindCamera()
  }

  fun showQrCodeScanner() {
    previewView.visibility = View.VISIBLE
    viewFinderImageView.visibility = View.VISIBLE
    qrCodeScannerLifecycle.bindCamera()
  }
}
