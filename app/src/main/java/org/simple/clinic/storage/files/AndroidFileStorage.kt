package org.simple.clinic.storage.files

import android.app.Application
import org.simple.clinic.storage.files.ClearAllFilesResult.Failure
import org.simple.clinic.storage.files.ClearAllFilesResult.PartiallyDeleted
import org.simple.clinic.storage.files.ClearAllFilesResult.Success
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class AndroidFileStorage @Inject constructor(
    private val application: Application,
    private val fileOperations: FileOperations
) : FileStorage {

  override fun getFile(filePath: String) = try {
    val file = application.filesDir.resolve(filePath)
    fileOperations.createFileIfItDoesNotExist(file)

    if (file.isFile) GetFileResult.Success(file) else GetFileResult.NotAFile(filePath)
  } catch (e: Throwable) {
    GetFileResult.Failure(e)
  }

  override fun writeToFile(file: File, text: String) = try {
    file
        .bufferedWriter()
        .use { writer -> writer.write(text) }

    WriteFileResult.Success(file)
  } catch (e: Throwable) {
    WriteFileResult.Failure(e)
  }

  override fun readFromFile(file: File) = try {
    val content = file
        .bufferedReader()
        .use { reader -> reader.readText() }

    ReadFileResult.Success(content)
  } catch (e: Throwable) {
    ReadFileResult.Failure(e)
  }

  override fun delete(file: File): DeleteFileResult = try {
    val isDeleted = file.delete()
    if (isDeleted.not()) throw RuntimeException("File: ${file.name} could not be deleted")

    DeleteFileResult.Success
  } catch (e: Throwable) {
    DeleteFileResult.Failure(e)
  }

  override fun clearAllFiles() = try {
    if (fileOperations.deleteContents(application.filesDir)) Success else PartiallyDeleted
  } catch (e: Throwable) {
    Failure(e)
  }

  override fun writeStreamToFile(inputStream: InputStream, file: File, bufferSize: Int) {
    inputStream.use { inputStreamSafe ->
      file.outputStream().use { outputStream ->
        inputStreamSafe.copyTo(outputStream, bufferSize)
      }
    }
  }
}
