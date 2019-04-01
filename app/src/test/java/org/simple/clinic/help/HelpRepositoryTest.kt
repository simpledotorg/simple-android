package org.simple.clinic.help

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.storage.files.DeleteFileResult
import org.simple.clinic.storage.files.FileStorage
import org.simple.clinic.storage.files.GetFileResult
import org.simple.clinic.storage.files.WriteFileResult
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toOptional
import java.io.File

class HelpRepositoryTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val fileStorage = mock<FileStorage>()
  private val file = mock<File>()
  private val helpFileName = "test.html"
  private val repository = HelpRepository(fileStorage, helpFileName)

  @Test
  fun `updating the help should save it to the file`() {
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.Success(file))
    whenever(fileStorage.writeToFile(any(), any())).thenReturn(WriteFileResult.Success(file))

    val helpContent = "Help contents"
    repository.updateHelp(helpContent).blockingAwait()

    verify(fileStorage).writeToFile(file, helpContent)
  }

  @Test
  fun `getting an empty file should emit None`() {
    whenever(file.length()).thenReturn(0L)
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.Success(file))

    val fileStreamObserver = repository.helpFile().test()

    fileStreamObserver.assertValue(None)
  }

  @Test
  fun `when the help file path is not a file, it should emit None`() {
    whenever(file.length()).thenReturn(0L)
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.NotAFile("test"))

    val repository = HelpRepository(fileStorage, "test")

    val fileStreamObserver = repository.helpFile().test()

    fileStreamObserver.assertValue(None)
  }

  @Test
  fun `when subscribing to the help file stream, the initial event should be emitted with the file`() {
    whenever(file.length()).thenReturn(1L)
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.Success(file))

    val fileStreamObserver = repository.helpFile().test()

    fileStreamObserver.assertValue(file.toOptional())
  }

  @Test
  fun `when the help file is updated, the change notification should publish changes`() {
    whenever(file.length()).thenReturn(1L)
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.Success(file))
    whenever(fileStorage.writeToFile(any(), any())).thenReturn(WriteFileResult.Success(file))

    val fileStreamObserver = repository.helpFile().test()

    repository.updateHelp("new help 1").blockingAwait()

    fileStreamObserver.assertValues(
        file.toOptional(),
        file.toOptional())

    repository.updateHelp("new help 2").blockingAwait()

    fileStreamObserver.assertValues(
        file.toOptional(),
        file.toOptional(),
        file.toOptional())
  }

  @Test
  fun `when writing to the help file fails, change notification should not be published`() {
    whenever(file.length()).thenReturn(1L)
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.Success(file))
    whenever(fileStorage.writeToFile(any(), any()))
        .thenReturn(WriteFileResult.Success(file), WriteFileResult.Failure(RuntimeException("Test Exception!")))

    val fileStreamObserver = repository.helpFile().test()

    repository.updateHelp("new help 1").blockingAwait()

    fileStreamObserver.assertValues(
        file.toOptional(),
        file.toOptional())

    repository.updateHelp("new help 2").blockingAwait()

    fileStreamObserver.assertValues(
        file.toOptional(),
        file.toOptional())
  }

  @Test
  fun `when file is deleted then trigger a None file notification`() {
    whenever(fileStorage.delete(file)).thenReturn(DeleteFileResult.Success)
    whenever(file.length()).thenReturn(1)
    whenever(fileStorage.getFile(helpFileName)).thenReturn(GetFileResult.Success(file))

    val helpFileStream = repository.helpFile().test()
    repository.deleteHelpFile().test().assertNoErrors()

    helpFileStream.assertValues(file.toOptional(), None)
  }
}
