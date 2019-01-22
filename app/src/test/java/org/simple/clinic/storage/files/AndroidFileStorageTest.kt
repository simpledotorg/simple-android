package org.simple.clinic.storage.files

import android.app.Application
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException

@RunWith(JUnitParamsRunner::class)
class AndroidFileStorageTest {

  @Test
  @Parameters(method = "params for exceptions thrown when getting file")
  fun `exceptions thrown when creating a file must be wrapped in the failure result`(cause: Throwable) {
    val application = mock<Application>()
    val applicationFilesDirectory = File("")
    whenever(application.filesDir).thenReturn(applicationFilesDirectory)

    val fileCreator = mock<FileCreator>()
    whenever(fileCreator.createFileIfItDoesNotExist(any())).thenThrow(cause)

    val fileStorage = AndroidFileStorage(application, fileCreator)

    val fileToCreate = applicationFilesDirectory.resolve("test.txt")
    val result = fileStorage.getFile(filePath = fileToCreate.path)

    assertThat(result).isEqualTo(GetFileResult.Failure(cause))
  }

  fun `params for exceptions thrown when getting file`(): List<Throwable> {
    return listOf(
        IOException("IO Error!"),
        SecurityException("You shall not pass!"),
        RuntimeException("Oh no, something went wrong!"))
  }
}
