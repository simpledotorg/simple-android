package org.simple.clinic.storage.files

import java.io.File
import java.io.InputStream
import java.io.OutputStream

sealed class GetFileResult {
  data class Success(val file: File) : GetFileResult()
  data class NotAFile(val name: String) : GetFileResult()
  data class Failure(val cause: Throwable) : GetFileResult()
}

sealed class WriteFileResult {
  data class Success(val file: File) : WriteFileResult()
  data class Failure(val cause: Throwable) : WriteFileResult()
}

sealed class ReadFileResult {
  data class Success(val content: String) : ReadFileResult()
  data class Failure(val cause: Throwable) : ReadFileResult()
}

sealed class DeleteFileResult {
  object Success : DeleteFileResult()
  data class Failure(val cause: Throwable) : DeleteFileResult()
}

sealed class ClearAllFilesResult {
  object Success : ClearAllFilesResult()
  object PartiallyDeleted : ClearAllFilesResult()
  data class Failure(val cause: Throwable) : ClearAllFilesResult()
}

interface FileStorage {

  fun getFile(filePath: String): GetFileResult

  fun writeToFile(file: File, text: String): WriteFileResult

  fun readFromFile(file: File): ReadFileResult

  fun delete(file: File): DeleteFileResult

  fun clearAllFiles(): ClearAllFilesResult

  fun copyTo(inputStream: InputStream, outputStream: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE)
}
