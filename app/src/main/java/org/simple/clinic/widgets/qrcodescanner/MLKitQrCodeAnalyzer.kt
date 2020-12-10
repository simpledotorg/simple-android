package org.simple.clinic.widgets.qrcodescanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class MLKitQrCodeAnalyzer(
    private val onQrCodeDetected: OnQrCodeDetected
) : ImageAnalysis.Analyzer {

  private val options = BarcodeScannerOptions.Builder()
      .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
      .build()
  private val scanner = BarcodeScanning.getClient(options)


  @SuppressLint("UnsafeExperimentalUsageError")
  override fun analyze(imageProxy: ImageProxy) {
    val mediaImage = imageProxy.image

    if (mediaImage == null) {
      imageProxy.close()
      return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    processImage(image = image, imageProxy = imageProxy)
  }

  private fun processImage(image: InputImage, imageProxy: ImageProxy) {
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
          barcodes.forEach { barcode ->
            val qrCodeResult = barcode.rawValue
            if (qrCodeResult != null) {
              onQrCodeDetected(qrCodeResult)
            }
          }
        }
        .addOnFailureListener {
          // Do nothing
        }
        .addOnCompleteListener {
          imageProxy.close()
        }
  }
}
