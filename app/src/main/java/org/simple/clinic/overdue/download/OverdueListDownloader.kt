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
import org.simple.clinic.util.CsvToPdfConverter
import org.simple.clinic.util.UserClock
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class OverdueListDownloader @Inject constructor(
    private val api: OverdueListDownloadApi,
    private val userClock: UserClock,
    private val appContext: Application,
    private val csvToPdfConverter: CsvToPdfConverter
) {

  companion object {
    private const val DOWNLOAD_FILE_NAME_PREFIX = "overdue-list-"
  }

  fun download(downloadFormat: OverdueListDownloadFormat): Single<Uri> {
    return api
        .download()
        .map { responseBody ->
          val localDateNow = LocalDate.now(userClock)
          val fileExtension = when (downloadFormat) {
            OverdueListDownloadFormat.CSV -> "csv"
            OverdueListDownloadFormat.PDF -> "pdf"
          }
          val fileName = "$DOWNLOAD_FILE_NAME_PREFIX$localDateNow.$fileExtension"

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadApi29(fileName, responseBody, downloadFormat)
          } else {
            downloadApi21(fileName, responseBody, downloadFormat)
          }
        }
        .flatMap { path ->
          scanFile(path, downloadFormat)
        }
  }

  private fun scanFile(
      path: String,
      downloadFormat: OverdueListDownloadFormat
  ) = Single.create<Uri> { emitter ->
    val mimeType = when (downloadFormat) {
      OverdueListDownloadFormat.CSV -> "text/csv"
      OverdueListDownloadFormat.PDF -> "application/pdf"
    }

    MediaScannerConnection.scanFile(appContext, arrayOf(path), arrayOf(mimeType)) { _, uri ->
      emitter.onSuccess(uri)
    }
  }

  private fun downloadApi21(
      fileName: String,
      responseBody: ResponseBody,
      downloadFormat: OverdueListDownloadFormat
  ): String {
    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsFolder, fileName)
    val outputStream = file.outputStream()

    when (downloadFormat) {
      OverdueListDownloadFormat.CSV -> responseBody.use {
        outputStream.use {
          responseBody.byteStream().copyTo(it)
        }
      }

      OverdueListDownloadFormat.PDF -> csvToPdfConverter.convert(
          responseBody.byteStream(),
          outputStream
      )
    }

    return file.path
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun downloadApi29(
      fileName: String,
      responseBody: ResponseBody,
      downloadFormat: OverdueListDownloadFormat
  ): String {
    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val file = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, fileName)
    }
    val fileUri = appContext.contentResolver.insert(collection, file)
        ?: throw Exception("MediaStore Uri couldn't be created")

    val outputStream = appContext.contentResolver.openOutputStream(fileUri, "w")
        ?: throw Exception("ContentResolver couldn't open $fileUri outputStream")

    when (downloadFormat) {
      OverdueListDownloadFormat.CSV -> responseBody.use {
        outputStream.use {
          responseBody.byteStream().copyTo(it)
        }
      }

      OverdueListDownloadFormat.PDF -> csvToPdfConverter.convert(
          responseBody.byteStream(),
          outputStream
      )
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
