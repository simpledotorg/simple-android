package org.simple.clinic.summary.assignedfacility

import org.simple.clinic.mobius.ViewRenderer

class AssignedFacilityUiRenderer(
    private val ui: AssignedFacilityUi
) : ViewRenderer<AssignedFacilityModel> {

  override fun render(model: AssignedFacilityModel) {
    if (model.hasAssignedFacility) {
      ui.renderAssignedFacilityName(model.assignedFacility!!.name)
    }
  }
}
