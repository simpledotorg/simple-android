package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientEntryValidationError
import org.simple.clinic.patient.PatientEntryValidationError.FULL_NAME_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.Optional
import org.simple.clinic.util.ValueChangedCallback
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE

class PatientEntryUiRenderer(
    val ui: PatientEntryUi,
    private val validationActions: PatientEntryValidationActions
) : ViewRenderer<PatientEntryModel> {
  private val dateOfBirthAndAgeVisibilityValueChangedCallback = ValueChangedCallback<DateOfBirthAndAgeVisibility>()
  private val identifierValueChangedCallback = ValueChangedCallback<Optional<Identifier>>()

  override fun render(model: PatientEntryModel) {
    val patientEntry = model.patientEntry
    identifierValueChangedCallback.pass(patientEntry.identifier.toOptional()) { renderIdentifier(patientEntry.identifier) }

    val personalDetails = patientEntry.personalDetails ?: return
    changeDateOfBirthAndAgeVisibility(personalDetails)
    model.validationError?.let { showValidationErrorUi(it) }
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

  private fun showValidationErrorUi(error: List<PatientEntryValidationError>) {
    error
        .forEach {
          when (it) {
            FULL_NAME_EMPTY -> validationActions.showEmptyFullNameError(true)
            PHONE_NUMBER_LENGTH_TOO_SHORT -> validationActions.showLengthTooShortPhoneNumberError(true)
          }
        }
  }
}
