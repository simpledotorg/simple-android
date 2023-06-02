package org.simple.clinic.patient.download.formatdialog

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.patient.download.PatientLineListFileFormat

class SelectLineListUiRendererTest {

  @Test
  fun `when file format changes, then update the ui`() {
    // given
    val ui = mock<SelectLineListUi>()
    val uiRenderer = SelectLineListUiRenderer(ui)
    val model = SelectLineListFormatModel
        .create()
        .fileFormatChanged(PatientLineListFileFormat.CSV)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setLineListFormat(PatientLineListFileFormat.CSV)
    verifyNoMoreInteractions(ui)
  }
}
