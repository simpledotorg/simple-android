package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.Optional
import org.simple.clinic.util.ValueChangedCallback
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE

class PatientEntryUiRenderer(val ui: PatientEntryUi) : ViewRenderer<PatientEntryModel> {
  private val dateOfBirthAndAgeVisibilityValueChangedCallback = ValueChangedCallback<DateOfBirthAndAgeVisibility>()
  private val identifierValueChangedCallback = ValueChangedCallback<Optional<Identifier>>()

  override fun render(model: PatientEntryModel) {
    val patientEntry = model.patientEntry
    identifierValueChangedCallback.pass(patientEntry.identifier.toOptional()) { renderIdentifier(patientEntry.identifier) }

    if (model.hasColonyOrVillagesList) {
      ui.setColonyOrVillagesAutoComplete(model.colonyOrVillagesList!!)
    }

    val personalDetails = patientEntry.personalDetails ?: return
    changeDateOfBirthAndAgeVisibility(personalDetails)

    buttonVisibility(model)
  }

  private fun buttonVisibility(model: PatientEntryModel) {
    if (model.nextButtonState == ButtonState.SAVING) {
      ui.nextButtonShowInProgress()
    } else {
      ui.enableNextButton()
    }
  }

  private fun renderIdentifier(identifier: Identifier?) {
    if (identifier != null) {
      ui.showIdentifierSection()
    } else {
      ui.hideIdentifierSection()
    }
  }

  private fun changeDateOfBirthAndAgeVisibility(personalDetails: OngoingNewPatientEntry.PersonalDetails) {
    val dateOfBirth = personalDetails.dateOfBirth
    val age = personalDetails.age

    dateOfBirthAndAgeVisibilityValueChangedCallback.pass(getVisibility(age, dateOfBirth), ui::setDateOfBirthAndAgeVisibility)
  }

  private fun getVisibility(age: String?, dateOfBirth: String?): DateOfBirthAndAgeVisibility {
    return if (age.isNullOrBlank() && dateOfBirth.isNullOrBlank()) {
      BOTH_VISIBLE
    } else if (age?.isNotBlank() == true && dateOfBirth?.isNotBlank() == true) {
      throw AssertionError("Both date-of-birth and age cannot have user input at the same time")
    } else if (age?.isNotBlank() == true) {
      AGE_VISIBLE
    } else {
      DATE_OF_BIRTH_VISIBLE
    }
  }
}
