package org.simple.clinic.editpatient

import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.widgets.UiEvent

sealed class EditPatientEvent : UiEvent

data class ScreenCreated( // TODO(rj): 2019-09-26 Move this class to 'editpatient_old' package
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?
) : EditPatientEvent() {
  companion object {
    fun from(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?
    ): ScreenCreated {
      return ScreenCreated(
          patient,
          address,
          phoneNumber
      )
    }
  }
}

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

class SaveClicked: EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Save Clicked"
}

class BackClicked: EditPatientEvent() {
  override val analyticsName = "Edit Patient Entry:Back Clicked"
}
