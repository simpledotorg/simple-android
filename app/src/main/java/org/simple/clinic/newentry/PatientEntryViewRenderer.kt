package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.DistinctValueCallback
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE

class PatientEntryViewRenderer(val ui: PatientEntryUi) : ViewRenderer<PatientEntryModel> {
  private val distinctDateOfBirthAndAgeVisibilityCallback: DistinctValueCallback<DateOfBirthAndAgeVisibility> = DistinctValueCallback()

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

    distinctDateOfBirthAndAgeVisibilityCallback.pass(getVisibility(age, dateOfBirth), ui::setDateOfBirthAndAgeVisibility)
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
