package org.simple.clinic.widgets.qrcodescanner

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.ImageFormat.YUV_422_888
import android.graphics.ImageFormat.YUV_444_888
import android.os.Build
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

private fun ByteBuffer.toByteArray(): ByteArray {
  rewind()
  val data = ByteArray(remaining())
  get(data)
  return data
}

class ZxingQrCodeAnalyzer(
    private val onQrCodeDetected: OnQrCodeDetected
) : ImageAnalysis.Analyzer {

  private val reader = MultiFormatReader().apply {
    val map = mapOf(
        DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)
    )
    setHints(map)
  }
  private val yuvFormats = mutableListOf(YUV_420_888)

  init {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      yuvFormats.addAll(listOf(YUV_422_888, YUV_444_888))
    }
  }

  override fun analyze(image: ImageProxy) {
    // We are using YUV format because, ImageProxy internally uses ImageReader to get the image.
    // By default ImageReader uses YUV format unless changed.
    // https://developer.android.com/reference/androidx/camera/core/ImageProxy.html#getImage()
    // https://developer.android.com/reference/android/media/Image.html#getFormat()
    if (image.format !in yuvFormats) {
      image.close()
      return
    }

    // First plane is the luminance plane for the image
    val data = image.planes[0].buffer.toByteArray()
    val source = PlanarYUVLuminanceSource(
        data,
        image.width,
        image.height,
        0,
        0,
        image.width,
        image.height,
        false
    )

    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
    try {
      val result = reader.decode(binaryBitmap)
      onQrCodeDetected(result.text)
    } catch (e: NotFoundException) {
      // NotFoundException will be thrown if a QR code is not detected
    }
    image.close()
  }
}
