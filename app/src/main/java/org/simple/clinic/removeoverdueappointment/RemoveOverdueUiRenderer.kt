package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.mobius.ViewRenderer

class RemoveOverdueUiRenderer(private val ui: RemoveOverdueUi) : ViewRenderer<RemoveOverdueModel> {

  private val allRemoveAppointmentReasons = RemoveAppointmentReason.values().toList()

  override fun render(model: RemoveOverdueModel) {
    ui.renderAppointmentRemoveReasons(allRemoveAppointmentReasons, model.selectedReason)

    renderDoneButton(model)
  }

  private fun renderDoneButton(model: RemoveOverdueModel) {
    if (!model.hasSelectedReason)
      ui.disableDoneButton()
    else
      ui.enableDoneButton()
  }
}
