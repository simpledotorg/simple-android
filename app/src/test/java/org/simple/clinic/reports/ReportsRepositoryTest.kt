package org.simple.clinic.reports

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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

class ReportsRepositoryTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val fileStorage = mock<FileStorage>()
  private val file = mock<File>()
  private val reportsFileName = "test.html"
  private val repository = ReportsRepository(fileStorage, reportsFileName)

  @Test
  fun `updating the reports should save it to the file`() {
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.Success(file))
    whenever(fileStorage.writeToFile(any(), any())).thenReturn(WriteFileResult.Success(file))

    val reportContent = "Report contents"
    repository.updateReports(reportContent).blockingAwait()

    verify(fileStorage).writeToFile(file, reportContent)
  }

  @Test
  fun `getting an empty file should emit None`() {
    whenever(file.length()).thenReturn(0L)
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.Success(file))

    val fileStreamObserver = repository.reportsFile().test()

    fileStreamObserver.assertValue(None)
  }

  @Test
  fun `when the reports file path is not a file, it should emit None`() {
    whenever(file.length()).thenReturn(0L)
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.NotAFile("test"))

    val repository = ReportsRepository(fileStorage, "test")

    val fileStreamObserver = repository.reportsFile().test()

    fileStreamObserver.assertValue(None)
  }

  @Test
  fun `when subscribing to the reports file stream, the initial event should be emitted with the file`() {
    whenever(file.length()).thenReturn(1L)
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.Success(file))

    val fileStreamObserver = repository.reportsFile().test()

    fileStreamObserver.assertValue(file.toOptional())
  }

  @Test
  fun `when the reports file is updated, the change notification should publish changes`() {
    whenever(file.length()).thenReturn(1L)
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.Success(file))
    whenever(fileStorage.writeToFile(any(), any())).thenReturn(WriteFileResult.Success(file))

    val fileStreamObserver = repository.reportsFile().test()

    repository.updateReports("new reports 1").blockingAwait()

    fileStreamObserver.assertValues(
        file.toOptional(),
        file.toOptional())

    repository.updateReports("new reports 2").blockingAwait()

    fileStreamObserver.assertValues(
        file.toOptional(),
        file.toOptional(),
        file.toOptional())
  }

  @Test
  fun `when writing to the reports file fails, change notification should not be published`() {
    whenever(file.length()).thenReturn(1L)
    whenever(fileStorage.getFile(any())).thenReturn(GetFileResult.Success(file))
    whenever(fileStorage.writeToFile(any(), any()))
        .thenReturn(WriteFileResult.Success(file), WriteFileResult.Failure(RuntimeException("Test Exception!")))

    val fileStreamObserver = repository.reportsFile().test()

    repository.updateReports("new reports 1").blockingAwait()

    fileStreamObserver.assertValues(
        file.toOptional(),
        file.toOptional())

    repository.updateReports("new reports 2").blockingAwait()

    fileStreamObserver.assertValues(
        file.toOptional(),
        file.toOptional())
  }

  @Test
  fun `when file is deleted then trigger a None file notification`() {
    whenever(fileStorage.delete(file)).thenReturn(DeleteFileResult.Success)
    whenever(file.length()).thenReturn(1)
    whenever(fileStorage.getFile(reportsFileName)).thenReturn(GetFileResult.Success(file))

    val reportsFileStream = repository.reportsFile().test()
    repository.deleteReportsFile().test().assertNoErrors()

    reportsFileStream.assertValues(file.toOptional(), None)
  }
}
