package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.download.OverdueListDownloadFormat
import org.simple.clinic.overdue.download.OverdueListDownloader
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class SelectOverdueDownloadFormatEffectHandlerTest {

  private val overdueListDownloader = mock<OverdueListDownloader>()
  private val uiActions = mock<UiActions>()
  private val viewEffectHandler = SelectOverdueDownloadFormatViewEffectHandler(uiActions)
  private val effectHandler = SelectOverdueDownloadFormatEffectHandler(
      overdueListDownloader = overdueListDownloader,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      viewEffectsConsumer = viewEffectHandler::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when download for share effect is received, then download the file`() {
    // given
    val format = OverdueListDownloadFormat.CSV
    val downloadedUri = mock<Uri>()

    whenever(overdueListDownloader.download(format)) doReturn Single.just(downloadedUri)

    // when
    testCase.dispatch(DownloadForShare(format))

    // then
    testCase.assertOutgoingEvents(FileDownloadedForSharing(downloadedUri))
  }
}
