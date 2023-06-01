package org.simple.clinic.overdue.download.formatdialog

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV

class SelectOverdueDownloadFormatUiRendererTest {

  private val ui = mock<SelectOverdueDownloadFormatUi>()
  private val uiRenderer = SelectOverdueDownloadFormatUiRenderer(ui)

  @Test
  fun `when dialog is opened for download, then render download ui`() {
    // given
    val model = SelectOverdueDownloadFormatModel.create(Download)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showTitle()
    verify(ui).showContent()
    verify(ui).hideProgress()
    verify(ui).showDownloadOrShareButton()
    verify(ui).setDownloadTitle()
    verify(ui).setDownloadButtonLabel()
    verify(ui).setOverdueListFormat(model.overdueListFileFormat)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when dialog is opened for share, then render share ui`() {
    // given
    val model = SelectOverdueDownloadFormatModel.create(Share)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showTitle()
    verify(ui).showContent()
    verify(ui).hideProgress()
    verify(ui).showDownloadOrShareButton()
    verify(ui).setShareTitle()
    verify(ui).setShareButtonLabel()
    verify(ui).setOverdueListFormat(model.overdueListFileFormat)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when download for share is in progress, then show in progress ui`() {
    // given
    val downloadForShareInProgress = SelectOverdueDownloadFormatModel
        .create(Share)
        .overdueListDownloadFormatUpdated(CSV)
        .overdueDownloadInProgress()

    // when
    uiRenderer.render(downloadForShareInProgress)

    // then
    verify(ui).hideTitle()
    verify(ui).hideContent()
    verify(ui).hideDownloadOrShareButton()
    verify(ui).showProgress()
  }
}
