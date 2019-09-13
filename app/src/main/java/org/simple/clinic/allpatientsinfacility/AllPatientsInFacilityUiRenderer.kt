package org.simple.clinic.allpatientsinfacility

import org.simple.clinic.mobius.ViewRenderer

class AllPatientsInFacilityUiRenderer(
    private val ui: AllPatientsInFacilityUi
) : ViewRenderer<AllPatientsInFacilityModel> {
  override fun render(model: AllPatientsInFacilityModel) {
    if (model.patientsQueried.not()) return

    if (model.patients.isNotEmpty()) {
      ui.showPatients(model.facilityUiState!!, model.patients)
    } else {
      ui.showNoPatientsFound(model.facilityUiState!!.name)
    }
  }
}
