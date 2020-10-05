package org.simple.clinic.teleconsultlog.shareprescription

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.FileDateTime
import org.simple.clinic.util.UserClock
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TeleconsultSharePrescriptionRepository @Inject constructor(
    private val appContext: Application,
    private val userClock: UserClock,
    @DateFormatter(FileDateTime) private val fileDateTimeFormatter: DateTimeFormatter
) {

  fun savePrescriptionBitmap(bitmap: Bitmap?) {
    val currentDateTime = LocalDateTime.now(userClock)
    val fileDateTime = fileDateTimeFormatter.format(currentDateTime)
    val fileName = "Simple prescription $fileDateTime.png"
    val imageOutputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      getFileOutputStreamUsingContentResolver(fileName)
    } else {
      getFileOutputStream(fileName)
    }

    imageOutputStream.use { outputStream ->
      bitmap?.compress(Bitmap.CompressFormat.PNG, 100, imageOutputStream)
      outputStream?.flush()
    }
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun getFileOutputStreamUsingContentResolver(fileName: String): OutputStream? {
    val resolver = appContext.contentResolver
    val contentValues = ContentValues()
    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
    val imageUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

    return if (imageUri != null) {
      resolver.openOutputStream(imageUri)
    } else {
      throw IllegalArgumentException("Failed to save teleconsult prescription image")
    }
  }

  private fun getFileOutputStream(fileName: String): OutputStream {
    val imagesDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS).toString()
    val file = File(imagesDir)

    val image = File(file, fileName)
    return FileOutputStream(image)
  }
}
