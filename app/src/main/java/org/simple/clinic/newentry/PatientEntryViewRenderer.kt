package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE

class PatientEntryViewRenderer(val ui: PatientEntryUi) : ViewRenderer<PatientEntryModel> {
  override fun render(model: PatientEntryModel) {
    val patientEntry = model.patientEntry ?: return

    if (patientEntry.identifier != null) {
      ui.showIdentifierSection()
    } else {
      ui.hideIdentifierSection()
    }

    val personalDetails = patientEntry.personalDetails ?: return
    val age = personalDetails.age
    val dateOfBirth = personalDetails.dateOfBirth

    if (age.isNullOrBlank() && dateOfBirth.isNullOrBlank()) {
      ui.setDateOfBirthAndAgeVisibility(BOTH_VISIBLE)
    } else if (age?.isNotBlank() == true && dateOfBirth?.isNotBlank() == true) {
      throw AssertionError("Both date-of-birth and age cannot have user input at the same time")
    } else if (age?.isNotBlank() == true) {
      ui.setDateOfBirthAndAgeVisibility(AGE_VISIBLE)
    } else if (dateOfBirth?.isNotBlank() == true) {
      ui.setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
    }
  }
}
