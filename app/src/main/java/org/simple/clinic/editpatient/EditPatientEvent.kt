package org.simple.clinic.editpatient

import org.simple.clinic.patient.Gender
import org.simple.clinic.widgets.UiEvent

sealed class EditPatientEvent : UiEvent

data class NameChanged(val name: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Patient Name Text Changed"
}

data class PhoneNumberChanged(val phoneNumber: String) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Phone Number Text Changed"
}

data class GenderChanged(val gender: Gender) : EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Changed Gender"
}

data class ColonyOrVillageChanged(val colonyOrVillage: String): EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Colony Or Village Text Changed"
}

data class DistrictChanged(val district: String): EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:District Text Changed"
}

data class StateChanged(val state: String): EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:State Text Changed"
}

data class DateOfBirthFocusChanged(val hasFocus: Boolean): EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Focused on DOB Text Field"
}

data class DateOfBirthChanged(val dateOfBirth: String): EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:DOB Text Changed"
}

data class AgeChanged(val age: String): EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Age Text Changed"
}

object SaveClicked: EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Save Clicked"
}

object BackClicked: EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Back Clicked"
}
