package org.simple.clinic.editpatient

import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.widgets.UiEvent

data class EditPatientScreenCreated(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?
) : UiEvent {
  companion object {
    fun from(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?
    ): EditPatientScreenCreated {
      return EditPatientScreenCreated(
          patient,
          address,
          phoneNumber
      )
    }
  }
}

data class NameChanged(val name: String) : UiEvent {
  override val analyticsName = "Edit Patient Entry:Patient Name Text Changed"
}

data class PhoneNumberChanged(val phoneNumber: String) : UiEvent {
  override val analyticsName = "Edit Patient Entry:Phone Number Text Changed"
}

data class GenderChanged(val gender: Gender) : UiEvent {
  override val analyticsName = "Edit Patient Entry:Changed Gender"
}

data class ColonyOrVillageChanged(val colonyOrVillage: String): UiEvent {
  override val analyticsName = "Edit Patient Entry:Colony Or Village Text Changed"
}

data class DistrictChanged(val district: String): UiEvent {
  override val analyticsName = "Edit Patient Entry:District Text Changed"
}

data class StateChanged(val state: String): UiEvent {
  override val analyticsName = "Edit Patient Entry:State Text Changed"
}

data class DateOfBirthFocusChanged(val hasFocus: Boolean): UiEvent {
  override val analyticsName = "Edit Patient Entry:Focused on DOB Text Field"
}

data class DateOfBirthChanged(val dateOfBirth: String): UiEvent {
  override val analyticsName = "Edit Patient Entry:DOB Text Changed"
}

data class AgeChanged(val age: String): UiEvent {
  override val analyticsName = "Edit Patient Entry:Age Text Changed"
}

class SaveClicked: UiEvent {
  override val analyticsName = "Edit Patient Entry:Save Clicked"
}

class PatientEditBackClicked: UiEvent {
  override val analyticsName = "Edit Patient Entry:Back Clicked"
}
