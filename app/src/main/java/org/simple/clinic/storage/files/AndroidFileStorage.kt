package org.simple.clinic.storage.files

import android.app.Application
import java.io.File
import javax.inject.Inject

class AndroidFileStorage @Inject constructor(
    private val application: Application,
    private val fileCreator: FileCreator
) : FileStorage {

  override fun getFile(filePath: String) = try {
    val file = application.filesDir.resolve(filePath)
    fileCreator.createFileIfItDoesNotExist(file)

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
}
