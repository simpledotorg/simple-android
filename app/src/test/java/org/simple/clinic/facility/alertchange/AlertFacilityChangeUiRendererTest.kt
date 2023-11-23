package org.simple.clinic.facility.alertchange

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AlertFacilityChangeUiRendererTest {

  private val ui = mock<AlertFacilityChangeUi>()
  private val uiRenderer = AlertFacilityChangeUiRenderer(ui = ui)

  @Test
  fun `when facility is changed, then show facility change alert`() {
    // given
    val model = AlertFacilityChangeModel
        .default()
        .updateIsFacilityChanged(isFacilityChanged = true)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showFacilityChangeAlert()
  }

  @Test
  fun `when facility is not changed, then hide facility change alert`() {
    // given
    val model = AlertFacilityChangeModel
        .default()
        .updateIsFacilityChanged(isFacilityChanged = false)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideFacilityChangeAlert()
  }
}
