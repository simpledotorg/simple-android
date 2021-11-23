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
import org.simple.clinic.overdue.download.OverdueListDownloadFormat
import org.simple.clinic.overdue.download.OverdueListDownloader
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
    val format = OverdueListDownloadFormat.CSV

    whenever(overdueListDownloader.download(format)) doReturn Single.just(downloadedUri)

    // when
    testCase.dispatch(DownloadForShare(format))

    // then
    testCase.assertOutgoingEvents(FileDownloadedForSharing(downloadedUri))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when share downloaded file effect is received, then share the downloaded file`() {
    // when
    testCase.dispatch(ShareDownloadedFile(downloadedUri))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).shareDownloadedFile(downloadedUri)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when schedule download effect is received, then schedule the overdue list download`() {
    // given
    val format = OverdueListDownloadFormat.PDF

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
}
