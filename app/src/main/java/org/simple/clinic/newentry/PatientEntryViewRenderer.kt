package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewRenderer

class PatientEntryViewRenderer(val ui: PatientEntryUi) : ViewRenderer<PatientEntryModel> {
  override fun render(model: PatientEntryModel) {
    if (model.patientEntry == null) return

    if (model.patientEntry.identifier != null) {
      ui.showIdentifierSection()
    } else {
      ui.hideIdentifierSection()
    }
  }
}
