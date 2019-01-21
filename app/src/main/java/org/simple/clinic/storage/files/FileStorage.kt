package org.simple.clinic.storage.files

import java.io.File

sealed class GetFileResult {
  data class Success(val file: File) : GetFileResult()
  data class NotAFile(val name: String): GetFileResult()
  data class Failure(val cause: Throwable) : GetFileResult()
}

sealed class WriteFileResult {
  object Success : WriteFileResult()
  data class Failure(val cause: Throwable) : WriteFileResult()
}

sealed class ReadFileResult {
  data class Success(val content: String) : ReadFileResult()
  data class Failure(val cause: Throwable) : ReadFileResult()
}

interface FileStorage {

  fun getFile(filePath: String): GetFileResult

  fun writeToFile(file: File, text: String): WriteFileResult

  fun readFromFile(file: File): ReadFileResult
}
