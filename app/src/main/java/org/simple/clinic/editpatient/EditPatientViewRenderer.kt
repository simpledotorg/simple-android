package org.simple.clinic.editpatient

import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.EthiopiaMedicalRecordNumber
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.SriLankaNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.SriLankaPersonalHealthNumber
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

    if (model.isAddingHealthIDsFromEditPatientEnabled) {
      ui.showBPPassportButton()
    }

    fillFormFields(
        model.ongoingEntry,
        model.savedBangladeshNationalId,
        model.canAddNHID
    )
    displayBpPassports(model)
  }

  private fun displayNewlyAddedNHID(alternativeId: String) {
    if (alternativeId.isNotEmpty()) {
      ui.hideAddNHIDButton()
      ui.setAlternateIdContainer(Identifier(alternativeId, IndiaNationalHealthId), true)
    } else {
      ui.showIndiaNHIDLabel()
      ui.showAddNHIDButton()
    }
  }

  private fun displayBpPassports(model: EditPatientModel) {
    val newlyAddedBpPassports = model.ongoingEntry.bpPassports?.map { it.displayValue() }.orEmpty()
    val identifiers = model.bpPassports?.map { it.identifier.displayValue() }.orEmpty()

    ui.displayBpPassports(identifiers + newlyAddedBpPassports)
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
      alternateId: BusinessId?,
      canAddNHID: Boolean
  ) {
    with(ui) {
      setPatientName(ongoingEntry.name)
      setPatientPhoneNumber(ongoingEntry.phoneNumber)
      setGender(ongoingEntry.gender)
      setState(ongoingEntry.state)
      setDistrict(ongoingEntry.district)
      setStreetAddress(ongoingEntry.streetAddress)
      setZone(ongoingEntry.zone)
      setColonyOrVillage(ongoingEntry.colonyOrVillage)
      setAlternateIdIfNHIDIsUpdated(
          ongoingEntry,
          alternateId,
          canAddNHID
      )
    }

    val ageOrDateOfBirth = ongoingEntry.ageOrDateOfBirth
    when (ageOrDateOfBirth) {
      is EntryWithAge -> ui.setPatientAge(ageOrDateOfBirth.age)
      is EntryWithDateOfBirth -> ui.setPatientDateOfBirth(ageOrDateOfBirth.dateOfBirth)
    }.exhaustive()
  }

  private fun setAlternateIdIfNHIDIsUpdated(
      ongoingEntry: EditablePatientEntry,
      alternateId: BusinessId?,
      canAddNHID: Boolean
  ) {
    if (canAddNHID) {
      displayNewlyAddedNHID(ongoingEntry.alternativeId)
    } else {
      setAlternateId(alternateId, ongoingEntry)
    }
  }

  private fun setAlternateId(
      alternateId: BusinessId?,
      ongoingEntry: EditablePatientEntry
  ) {
    when (val alternateIdentifierType = alternateId?.identifier?.type) {
      // When alternative id is not present, we would want to set the text watcher on
      // text field, so that we can listen the text changes and update the model
      // accordingly. It is same as setting text changes observable on the text field.
      null,
      BangladeshNationalId,
      EthiopiaMedicalRecordNumber,
      SriLankaNationalId,
      SriLankaPersonalHealthNumber -> ui.setAlternateIdTextField(ongoingEntry.alternativeId)
      IndiaNationalHealthId -> ui.setAlternateIdContainer(Identifier(ongoingEntry.alternativeId, alternateIdentifierType), false)
      Identifier.IdentifierType.BpPassport,
      is Identifier.IdentifierType.Unknown -> throw IllegalArgumentException("Unknown alternative id: $alternateId")
    }.exhaustive()
  }
}
