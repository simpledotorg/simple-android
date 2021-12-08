package org.simple.clinic.editpatient

import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.exhaustive
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
      ui.setColonyOrVillagesAutoComplete(model.colonyOrVillagesList!!)
    }

    if (model.hasInputFields) {
      ui.setupUi(model.inputFields!!)
    }

    fillFormFields(model.ongoingEntry, model.savedBangladeshNationalId)
    displayBpPassports(model)
  }

  private fun displayBpPassports(model: EditPatientModel) {
    val identifiers = model.bpPassports?.map { it.identifier.displayValue() }.orEmpty()

    ui.displayBpPassports(identifiers)
  }

  private fun manageButtonState(model: EditPatientModel) {
    if (model.saveButtonState == EditPatientState.SAVING_PATIENT) {
      ui.showProgress()
    } else {
      ui.hideProgress()
    }
  }

  private fun fillFormFields(
      ongoingEntry: EditablePatientEntry,
      alternateId: BusinessId?
  ) {
    with(ui) {
      setPatientName(ongoingEntry.name)
      setGender(ongoingEntry.gender)
      setState(ongoingEntry.state)
      setDistrict(ongoingEntry.district)
      setStreetAddress(ongoingEntry.streetAddress)
      setZone(ongoingEntry.zone)
      setColonyOrVillage(ongoingEntry.colonyOrVillage)

      if (ongoingEntry.phoneNumber.isNotBlank()) {
        setPatientPhoneNumber(ongoingEntry.phoneNumber)
      }

      if (alternateId != null) {
        setAlternateId(ongoingEntry, alternateId)
      }
    }

    val ageOrDateOfBirth = ongoingEntry.ageOrDateOfBirth
    when (ageOrDateOfBirth) {
      is EntryWithAge -> ui.setPatientAge(ageOrDateOfBirth.age)
      is EntryWithDateOfBirth -> ui.setPatientDateOfBirth(ageOrDateOfBirth.dateOfBirth)
    }.exhaustive()
  }

  private fun setAlternateId(ongoingEntry: EditablePatientEntry, alternateId: BusinessId) {
    if (alternateId.identifier.value != ongoingEntry.alternativeId) {
      ui.setAlternateId(Identifier(ongoingEntry.alternativeId, alternateId.identifier.type))
    } else {
      ui.setAlternateId(alternateId.identifier)
    }
  }
}
