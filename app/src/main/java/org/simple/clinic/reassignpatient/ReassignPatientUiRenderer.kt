package org.simple.clinic.reassignpatient

import org.simple.clinic.mobius.ViewRenderer

class ReassignPatientUiRenderer(
    private val ui: ReassignPatientUi
) : ViewRenderer<ReassignPatientModel> {

  override fun render(model: ReassignPatientModel) {
    if (model.hasAssignedFacility) {
      ui.renderAssignedFacilityName(model.assignedFacility!!.name)
    }
  }
}
