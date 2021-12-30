package org.simple.clinic.summary.nextappointment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import java.util.UUID

class NextAppointmentUiRendererTest {

  @Test
  fun `when appointment is not present, then render no appointment view`() {
    // given
    val ui = mock<NextAppointmentUi>()
    val uiRenderer = NextAppointmentUiRenderer(ui)
    val defaultModel = NextAppointmentModel.default(
        patientUuid = UUID.fromString("305c6c33-90b5-4895-9f82-e6d071d05955")
    )

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showNoAppointment()
    verify(ui).showAddAppointmentButton()
    verifyNoMoreInteractions(ui)
  }
}
