package org.simple.clinic.storage.files

import java.io.File
import java.io.IOException
import javax.inject.Inject

class FileOperations @Inject constructor() {

  // Adding a @Throws annotation is necessary for Mockito
  @Throws(IOException::class)
  fun createFileIfItDoesNotExist(file: File) {
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

  // Adding a @Throws annotation is necessary for Mockito
  @Throws(IOException::class)
  fun deleteContents(file: File): Boolean {
    return file
        .listFiles()
        .map { it.deleteRecursively() }
        .fold(true) { allDeleted, currentFileDeleted -> allDeleted && currentFileDeleted }
  }
}
