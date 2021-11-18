package org.simple.clinic.overdue.download.formatdialog

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class SelectOverdueDownloadFormatUiRendererTest {

  @Test
  fun `when dialog is opened for download, then render download ui`() {
    // given
    val model = SelectOverdueDownloadFormatModel.create(Download)
    val ui = mock<SelectOverdueDownloadFormatUi>()
    val uiRenderer = SelectOverdueDownloadFormatUiRenderer(ui)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setDownloadTitle()
    verify(ui).setDownloadButtonLabel()
    verify(ui).setOverdueListFormat(model.overdueListDownloadFormat)
    verifyNoMoreInteractions(ui)
  }
}
