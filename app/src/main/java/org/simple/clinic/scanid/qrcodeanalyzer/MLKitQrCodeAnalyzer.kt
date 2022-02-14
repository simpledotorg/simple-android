package org.simple.clinic.scanid.qrcodeanalyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.dynamite.DynamiteModule.LoadingException
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
import com.google.mlkit.vision.common.InputImage
import org.simple.clinic.util.BitmapUtils

class MLKitQrCodeAnalyzer(
    private val bitmapUtils: BitmapUtils,
    private val onQrCodeDetected: OnQrCodeDetected,
    private val mlKitUnavailable: () -> Unit
) : ImageAnalysis.Analyzer {

  private val options = BarcodeScannerOptions.Builder()
      .setBarcodeFormats(FORMAT_QR_CODE)
      .build()
  private val scanner = BarcodeScanning.getClient(options)

  override fun analyze(imageProxy: ImageProxy) {
    val bitmap = bitmapUtils.getBitmap(imageProxy)
    if (bitmap == null) {
      imageProxy.close()
      return
    }

    val image = InputImage.fromBitmap(bitmap, 0)
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
        .addOnFailureListener(::handleException)
        .addOnCompleteListener {
          imageProxy.close()
        }
  }

  private fun handleException(exception: Exception) {
    val isMLKitUnavailable = exception is MlKitException && exception.errorCode == MlKitException.UNAVAILABLE
    if (exception is LoadingException || isMLKitUnavailable) {
      mlKitUnavailable()
    }
  }
}
