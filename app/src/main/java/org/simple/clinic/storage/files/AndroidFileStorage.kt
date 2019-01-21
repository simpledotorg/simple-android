package org.simple.clinic.storage.files

import android.app.Application
import java.io.File
import javax.inject.Inject

class AndroidFileStorage @Inject constructor(
    private val application: Application
) : FileStorage {
  override fun getFile(filePath: String) = try {
    val file = application.filesDir.resolve(filePath)
    createFileIfItDoesNotExist(file)

    if (file.isFile) GetFileResult.Success(file) else GetFileResult.NotAFile(filePath)
  } catch (e: Throwable) {
    GetFileResult.Failure(e)
  }

  override fun writeToFile(file: File, string: String): WriteFileResult {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun readFromFile(file: File): ReadFileResult {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun createFileIfItDoesNotExist(file: File) {
    if (!file.exists()) {
      val parentExists = file.parentFile.exists() || file.parentFile.mkdirs()
      if (parentExists) {
        val fileCreated = file.createNewFile()
        if (!fileCreated) {
          throw RuntimeException("Could not create file: ${file.path}")
        }

      } else {
        throw RuntimeException("Could not create directory: ${file.parentFile.path}")
      }
    }
  }
}
