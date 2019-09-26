package org.simple.clinic.editpatient

import org.simple.clinic.editpatient.OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE

class EditPatientViewRenderer(private val ui: EditPatientUi) : ViewRenderer<EditPatientModel> {
  override fun render(model: EditPatientModel) {
    val ageOrDateOfBirth = model.ongoingEntry.ageOrDateOfBirth

    when(ageOrDateOfBirth) {
      is EntryWithAge -> if (ageOrDateOfBirth.age.isBlank()) {
        ui.setDateOfBirthAndAgeVisibility(BOTH_VISIBLE)
      } else {
        ui.setDateOfBirthAndAgeVisibility(AGE_VISIBLE)
      }

      is EntryWithDateOfBirth -> if (ageOrDateOfBirth.dateOfBirth.isBlank()) {
        ui.setDateOfBirthAndAgeVisibility(BOTH_VISIBLE)
      } else {
        ui.setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)
      }
    }
  }
}
