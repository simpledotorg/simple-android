package org.simple.clinic.teleconsultlog.shareprescription

import android.app.Application
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
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

  fun savePrescriptionBitmap(bitmap: Bitmap?): String {
    val currentDateTime = LocalDateTime.now(userClock)
    val fileDateTime = fileDateTimeFormatter.format(currentDateTime)
    val fileName = "Simple prescription $fileDateTime.png"
    val imageOutputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      getFileOutputStreamUsingContentResolver(fileName)
    } else {
      getFileOutputStream(fileName)
    }

    imageOutputStream?.use { outputStream ->
      bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
      outputStream.flush()
    }
    return fileName
  }

  fun sharePrescription(fileName: String): Uri? {
    var contentUri: Uri? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val prescriptionUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
      val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)
      val selection = MediaStore.Images.Media.DISPLAY_NAME

      appContext.contentResolver.query(
          prescriptionUri,
          projection,
          "$selection = '$fileName'",
          null,
          null
      )?.use { cursor ->
        contentUri = getImageUri(cursor)
      }
      return contentUri
    } else {
      val filePath = File(Environment.getExternalStorageDirectory().absolutePath + "/${Environment.DIRECTORY_DOWNLOADS}/$fileName")
      contentUri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.provider", filePath)
      return contentUri
    }
  }

  private fun getImageUri(cursor: Cursor): Uri {
    var contentUri: Uri? = null
    val columnId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
    if (cursor.moveToNext()) {
      val id = cursor.getLong(columnId)

      contentUri = ContentUris.withAppendedId(
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          id
      )
    }
    return contentUri
        ?: throw IllegalArgumentException("Failed to retrieve teleconsult prescription image")
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
