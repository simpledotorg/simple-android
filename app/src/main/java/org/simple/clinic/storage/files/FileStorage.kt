package org.simple.clinic.storage.files

import java.io.File

sealed class GetFileResult {
  data class Success(val file: File) : GetFileResult()
  class Failure(val cause: Throwable) : GetFileResult()
}

sealed class WriteFileResult {
  object Success : WriteFileResult()
  class Failure(val cause: Throwable) : WriteFileResult()
}

sealed class ReadFileResult {
  data class Success(val content: String) : ReadFileResult()
  class Failure(val cause: Throwable) : ReadFileResult()
}

interface FileStorage {

  fun getWriteableFile(name: String): GetFileResult

  fun writeToFile(file: File, string: String): WriteFileResult

  fun readFromFile(file: File): ReadFileResult
}
