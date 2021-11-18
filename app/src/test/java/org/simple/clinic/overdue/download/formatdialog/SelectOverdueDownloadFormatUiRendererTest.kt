package org.simple.clinic.overdue.download.formatdialog

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

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
    verify(ui).setDownloadTitle()
    verify(ui).setDownloadButtonLabel()
    verify(ui).setOverdueListFormat(model.overdueListDownloadFormat)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when dialog is opened for share, then render share ui`() {
    // given
    val model = SelectOverdueDownloadFormatModel.create(Share)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setShareTitle()
    verify(ui).setShareButtonLabel()
    verify(ui).setOverdueListFormat(model.overdueListDownloadFormat)
    verifyNoMoreInteractions(ui)
  }
}
