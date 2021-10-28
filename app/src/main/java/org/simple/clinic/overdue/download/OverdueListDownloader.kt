package org.simple.clinic.overdue.download

import android.app.Application
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import io.reactivex.Single
import okhttp3.ResponseBody
import org.simple.clinic.util.UserClock
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class OverdueListDownloader @Inject constructor(
    private val api: OverdueListDownloadApi,
    private val userClock: UserClock,
    private val appContext: Application
) {

  companion object {
    private const val DOWNLOAD_FILE_NAME_PREFIX = "overdue-list-"
  }

  fun downloadAsCsv(): Single<Uri> {
    return Single.create { emitter ->
      try {
        val response = api.download().execute()
        val responseBody = response.body()!!

        val localDateNow = LocalDate.now(userClock)
        val fileName = "$DOWNLOAD_FILE_NAME_PREFIX$localDateNow.csv"

        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          downloadCsvApi29(fileName, responseBody)
        } else {
          downloadCsvApi21(fileName, responseBody)
        }

        MediaScannerConnection.scanFile(appContext, arrayOf(path), arrayOf("text/csv")) { _, uri ->
          emitter.onSuccess(uri)
        }
      } catch (e: Throwable) {
        emitter.onError(e)
      }
    }
  }

  private fun downloadCsvApi21(fileName: String, responseBody: ResponseBody): String {
    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsFolder, fileName)
    val outputStream = file.outputStream()

    responseBody.use {
      outputStream.use {
        responseBody.byteStream().copyTo(it)
      }
    }

    return file.path
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun downloadCsvApi29(fileName: String, responseBody: ResponseBody): String {
    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val file = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, fileName)
    }
    val fileUri = appContext.contentResolver.insert(collection, file)
        ?: throw Exception("MediaStore Uri couldn't be created")

    val outputStream = appContext.contentResolver.openOutputStream(fileUri, "w")
        ?: throw Exception("ContentResolver couldn't open $fileUri outputStream")

    responseBody.use {
      outputStream.use {
        responseBody.byteStream().copyTo(it)
      }
    }

    return getMediaStoreEntryPathApi29(fileUri)
        ?: throw Exception("ContentResolver couldn't find $fileUri")
  }

  private fun getMediaStoreEntryPathApi29(uri: Uri): String? {
    val cursor = appContext.contentResolver.query(
        uri,
        arrayOf(MediaStore.Files.FileColumns.DATA),
        null,
        null,
        null
    ) ?: return null

    return cursor.use {
      if (!cursor.moveToFirst()) {
        return@use null
      }

      return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
    }
  }
}
