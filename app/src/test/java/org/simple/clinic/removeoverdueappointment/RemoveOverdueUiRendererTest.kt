package org.simple.clinic.removeoverdueappointment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.contactpatient.RemoveAppointmentReason
import java.util.UUID

class RemoveOverdueUiRendererTest {

  private val ui = mock<RemoveOverdueUi>()
  private val uiRenderer = RemoveOverdueUiRenderer(ui)

  private val appointmentId = UUID.fromString("ce68996f-8b20-47b7-80c8-c0e6fdd5bda7")
  private val patientId = UUID.fromString("06b6f782-8298-4a0c-a0e9-bc30f5055991")
  private val appointment = TestData.appointment(
      uuid = appointmentId,
      patientUuid = patientId
  )
  private val defaultModel = RemoveOverdueModel.create(appointment)

  @Test
  fun `when a cancel reason is not selected, then done button must be disabled`() {
    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).renderAppointmentRemoveReasons(
        reasons = RemoveAppointmentReason.values().toList(),
        selectedReason = null
    )
    verify(ui).disableDoneButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when a cancel reason is selected, then done button must be enabled`() {
    // given
    val model = defaultModel.removeAppointmentReasonSelected(
        RemoveAppointmentReason.AlreadyVisited)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderAppointmentRemoveReasons(
        reasons = RemoveAppointmentReason.values().toList(),
        selectedReason = RemoveAppointmentReason.AlreadyVisited
    )
    verify(ui).enableDoneButton()
    verifyNoMoreInteractions(ui)
  }
}
