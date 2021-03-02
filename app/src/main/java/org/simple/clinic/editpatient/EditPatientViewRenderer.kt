package org.simple.clinic.editpatient

import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE

class EditPatientViewRenderer(private val ui: EditPatientUi) : ViewRenderer<EditPatientModel> {
  override fun render(model: EditPatientModel) {
    val ageOrDateOfBirth = model.ongoingEntry.ageOrDateOfBirth

    val fieldToShow = when {
      ageOrDateOfBirth.isBlank -> BOTH_VISIBLE
      ageOrDateOfBirth is EntryWithDateOfBirth -> DATE_OF_BIRTH_VISIBLE
      ageOrDateOfBirth is EntryWithAge -> AGE_VISIBLE
      else -> throw IllegalStateException("Unknown condition, this shouldn't happen: $ageOrDateOfBirth")
    }

    ui.setDateOfBirthAndAgeVisibility(fieldToShow)

    manageButtonState(model)

    if (model.hasColonyOrVillagesList) {
      ui.showColonyOrVillagesList(model.colonyOrVillagesList!!)
    }
  }

  private fun manageButtonState(model: EditPatientModel) {
    if (model.saveButtonState == EditPatientState.SAVING_PATIENT) {
      ui.showProgress()
    } else {
      ui.hideProgress()
    }
  }
}
