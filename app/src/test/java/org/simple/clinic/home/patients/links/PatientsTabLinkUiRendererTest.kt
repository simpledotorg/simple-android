package org.simple.clinic.home.patients.links

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class PatientsTabLinkUiRendererTest {
  private val ui = mock<PatientsTabLinkUi>()
  private val defaultModel = PatientsTabLinkModel.default()

  @Test
  fun `when patient line list download is enabled, then show patient line list download option`() {
    // given
    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = true
    )

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showOrHidePatientLineListDownload(true)
    verifyNoMoreInteractions(ui)
  }
}
