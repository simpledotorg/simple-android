package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.download.OverdueDownloadScheduler
import org.simple.clinic.overdue.download.OverdueListDownloadResult.DownloadSuccessful
import org.simple.clinic.overdue.download.OverdueListDownloader
import org.simple.clinic.overdue.download.OverdueListFileFormat
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class SelectOverdueDownloadFormatEffectHandlerTest {

  private val overdueListDownloader = mock<OverdueListDownloader>()
  private val uiActions = mock<UiActions>()
  private val viewEffectHandler = SelectOverdueDownloadFormatViewEffectHandler(uiActions)
  private val overdueDownloadScheduler = mock<OverdueDownloadScheduler>()
  private val effectHandler = SelectOverdueDownloadFormatEffectHandler(
      overdueListDownloader = overdueListDownloader,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      overdueDownloadScheduler = overdueDownloadScheduler,
      viewEffectsConsumer = viewEffectHandler::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)
  private val downloadedUri = mock<Uri>()

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when download for share effect is received, then download the file`() {
    // given
    val format = OverdueListFileFormat.CSV

    whenever(overdueListDownloader.downloadForShare(format)) doReturn Single.just(DownloadSuccessful(downloadedUri))

    // when
    testCase.dispatch(DownloadForShare(format))

    // then
    testCase.assertOutgoingEvents(FileDownloadedForSharing(DownloadSuccessful(downloadedUri)))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when share downloaded file effect is received, then share the downloaded file`() {
    // given
    val mimeType = "text/csv"

    // when
    testCase.dispatch(ShareDownloadedFile(downloadedUri, mimeType))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).shareDownloadedFile(downloadedUri, mimeType)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when schedule download effect is received, then schedule the overdue list download`() {
    // given
    val format = OverdueListFileFormat.PDF

    // when
    testCase.dispatch(ScheduleDownload(format))

    // given
    testCase.assertOutgoingEvents(OverdueDownloadScheduled)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when dismiss effect is received, then dismiss the dialog`() {
    // when
    testCase.dispatch(Dismiss)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).dismiss()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open not enough storage error dialog effect is received, then open not enough storage error dialog`() {
    // when
    testCase.dispatch(OpenNotEnoughStorageErrorDialog)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openNotEnoughStorageErrorDialog()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open download failed error dialog effect is received, then open download failed error dialog`() {
    // when
    testCase.dispatch(OpenDownloadFailedErrorDialog)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openDownloadFailedErrorDialog()
    verifyNoMoreInteractions(uiActions)
  }
}
