package org.simple.clinic.overdue.download

import android.app.Application
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import io.reactivex.Single
import okhttp3.ResponseBody
import org.simple.clinic.overdue.download.OverdueListDownloadResult.DownloadFailed
import org.simple.clinic.overdue.download.OverdueListDownloadResult.DownloadSuccessful
import org.simple.clinic.overdue.download.OverdueListDownloadResult.NotEnoughStorage
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
import org.simple.clinic.overdue.download.OverdueListFileFormat.PDF
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
    private const val MIN_REQ_SPACE = 10_00_0000L
  }

  fun download(fileFormat: OverdueListFileFormat): Single<OverdueListDownloadResult> {
    if (!hasMinReqSpace()) {
      return Single.just(NotEnoughStorage)
    }

    return api
        .download()
        .map { responseBody ->
          val localDateNow = LocalDate.now(userClock)
          val fileExtension = when (fileFormat) {
            CSV -> "csv"
            PDF -> "pdf"
          }
          val fileName = "$DOWNLOAD_FILE_NAME_PREFIX$localDateNow.$fileExtension"

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadApi29(fileName, responseBody, fileFormat)
          } else {
            downloadApi21(fileName, responseBody, fileFormat)
          }
        }
        .flatMap { path ->
          scanFile(path, fileFormat)
        }
        .map { uri -> DownloadSuccessful(uri) as OverdueListDownloadResult }
        .onErrorReturn { _ -> DownloadFailed }
  }

  private fun scanFile(
      path: String,
      fileFormat: OverdueListFileFormat
  ) = Single.create<Uri> { emitter ->
    MediaScannerConnection.scanFile(appContext, arrayOf(path), arrayOf(fileFormat.mimeType)) { _, uri ->
      emitter.onSuccess(uri)
    }
  }

  private fun downloadApi21(
      fileName: String,
      responseBody: ResponseBody,
      fileFormat: OverdueListFileFormat
  ): String {
    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsFolder, fileName)
    val outputStream = file.outputStream()

    when (fileFormat) {
      CSV -> responseBody.use {
        outputStream.use {
          responseBody.byteStream().copyTo(it)
        }
      }

      PDF -> csvToPdfConverter.convert(
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
      fileFormat: OverdueListFileFormat
  ): String {
    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val file = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, fileName)
    }
    val fileUri = appContext.contentResolver.insert(collection, file)
        ?: throw Exception("MediaStore Uri couldn't be created")

    val outputStream = appContext.contentResolver.openOutputStream(fileUri, "w")
        ?: throw Exception("ContentResolver couldn't open $fileUri outputStream")

    when (fileFormat) {
      CSV -> responseBody.use {
        outputStream.use {
          responseBody.byteStream().copyTo(it)
        }
      }

      PDF -> csvToPdfConverter.convert(
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

  private fun hasMinReqSpace(): Boolean {
    // If we cannot get access to the directory, we will allow saving to directory and fail with
    // error if there isn't enough space
    val dir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        ?: return true
    val statFs = StatFs(dir.path)
    val availableSpace = statFs.availableBlocksLong * statFs.blockSizeLong

    return availableSpace >= MIN_REQ_SPACE
  }
}
