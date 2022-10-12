package org.simple.clinic.patient.download

import android.app.Application
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dagger.Lazy
import io.reactivex.Single
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.Day
import org.simple.clinic.di.DateFormatter.Type.MonthName
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.download.PatientLineListDownloadResult.DownloadFailed
import org.simple.clinic.patient.download.PatientLineListDownloadResult.DownloadSuccessful
import org.simple.clinic.patient.download.PatientLineListDownloadResult.NotEnoughStorage
import org.simple.clinic.util.CsvToPdfConverter
import org.simple.clinic.util.UserClock
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PatientLineListDownloader @Inject constructor(
    private val appContext: Application,
    private val userClock: UserClock,
    private val patientLineListCsvGenerator: PatientLineListCsvGenerator,
    private val csvToPdfConverter: CsvToPdfConverter,
    private val currentFacility: Lazy<Facility>,
    @DateFormatter(Day) private val dateFormatter: DateTimeFormatter,
    @DateFormatter(MonthName) private val monthNameFormatter: DateTimeFormatter
) {

  companion object {
    private const val DOWNLOAD_FILE_NAME_PREFIX = "patient-line-list-"
    private const val MIN_REQ_SPACE = 10_00_0000L
  }

  fun download(
      fileFormat: PatientLineListFileFormat
  ): Single<PatientLineListDownloadResult> {
    if (!hasMinReqSpace()) {
      return Single.just(NotEnoughStorage)
    }

    return Single.create {
      val facility = currentFacility.get()
      val facilityId = facility.uuid
      val facilityName = facility.name

      try {
        val csvOutputStream = patientLineListCsvGenerator.generate(facilityId = facilityId)
        val csvInputStream = ByteArrayInputStream(csvOutputStream.toByteArray())

        it.onSuccess(saveFileToDisk(fileFormat, facilityName, csvInputStream))
      } catch (e: Throwable) {
        it.onError(e)
      }
    }.flatMap { path ->
      scanFile(path, fileFormat)
    }.map { uri ->
      DownloadSuccessful(uri) as PatientLineListDownloadResult
    }.onErrorReturn { _ ->
      DownloadFailed
    }
  }

  private fun saveFileToDisk(
      fileFormat: PatientLineListFileFormat,
      facilityName: String,
      inputStream: ByteArrayInputStream
  ): String {
    val fileName = generateFileName(fileFormat, facilityName)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      downloadApi29(fileName, inputStream, fileFormat)
    } else {
      downloadApi21(fileName, inputStream, fileFormat)
    }
  }

  private fun scanFile(
      path: String,
      fileFormat: PatientLineListFileFormat
  ) = Single.create<Uri> { emitter ->
    MediaScannerConnection.scanFile(appContext, arrayOf(path), arrayOf(fileFormat.mimeType)) { _, uri ->
      emitter.onSuccess(uri)
    }
  }

  private fun downloadApi21(
      fileName: String,
      inputStream: InputStream,
      fileFormat: PatientLineListFileFormat
  ): String {
    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsFolder, fileName)
    val outputStream = file.outputStream()

    writeResponseToOutputStream(fileFormat, inputStream, outputStream)

    return file.path
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun downloadApi29(
      fileName: String,
      inputStream: InputStream,
      fileFormat: PatientLineListFileFormat
  ): String {
    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val file = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, fileName)
    }
    val fileUri = appContext.contentResolver.insert(collection, file)
        ?: throw Exception("MediaStore Uri couldn't be created")

    val outputStream = appContext.contentResolver.openOutputStream(fileUri, "w")
        ?: throw Exception("ContentResolver couldn't open $fileUri outputStream")

    writeResponseToOutputStream(fileFormat, inputStream, outputStream)

    return getMediaStoreEntryPathApi29(fileUri)
        ?: throw Exception("ContentResolver couldn't find $fileUri")
  }

  private fun writeResponseToOutputStream(
      fileFormat: PatientLineListFileFormat,
      inputStream: InputStream,
      outputStream: OutputStream
  ) {
    when (fileFormat) {
      PatientLineListFileFormat.CSV -> inputStream.use { closableInputStream ->
        outputStream.use { closableOutputStream ->
          closableInputStream.copyTo(closableOutputStream)
        }
      }

      PatientLineListFileFormat.PDF -> csvToPdfConverter.convert(inputStream, outputStream)
    }
  }

  private fun generateFileName(
      fileFormat: PatientLineListFileFormat,
      facilityName: String
  ): String {
    val localDateNow = LocalDate.now(userClock)
    val fileExtension = when (fileFormat) {
      PatientLineListFileFormat.CSV -> "csv"
      PatientLineListFileFormat.PDF -> "pdf"
    }

    val date = dateFormatter.format(localDateNow)
    val monthName = monthNameFormatter.format(localDateNow)
    val formattedFacilityName = facilityName
        .replace(" ", "")
        .lowercase()

    return "${DOWNLOAD_FILE_NAME_PREFIX}$date-$monthName-$formattedFacilityName.$fileExtension"
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
