package org.simple.clinic.editpatient

import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.widgets.UiEvent

sealed class EditPatientEvent : UiEvent

data class BpPassportsFetched(val bpPasssports: List<BusinessId>) : EditPatientEvent()

data class NameChanged(val name: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Patient Name Text Changed"
}

data class PhoneNumberChanged(val phoneNumber: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Phone Number Text Changed"
}

data class GenderChanged(val gender: Gender) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Changed Gender"
}

data class ColonyOrVillageChanged(val colonyOrVillage: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Colony Or Village Text Changed"
}

data class DistrictChanged(val district: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:District Text Changed"
}

data class StateChanged(val state: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:State Text Changed"
}

data class DateOfBirthFocusChanged(val hasFocus: Boolean) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Focused on DOB Text Field"
}

data class DateOfBirthChanged(val dateOfBirth: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:DOB Text Changed"
}

data class AgeChanged(val age: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Age Text Changed"
}

data class ZoneChanged(val zone: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Zone Text Changed"
}

data class StreetAddressChanged(val streetAddress: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Street Address Text Changed"
}

object SaveClicked : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Save Clicked"
}

object PatientSaved : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Patient Saved"
}

object BackClicked : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Back Clicked"
}

data class AlternativeIdChanged(val alternativeId: String) : EditPatientEvent() {
  override val analyticsName: String = "Edit Patient Entry:Bangladesh National ID Changed"
}

data class InputFieldsLoaded(val inputFields: InputFields) : EditPatientEvent()

data class ColonyOrVillagesFetched(val colonyOrVillages: List<String>) : EditPatientEvent()
