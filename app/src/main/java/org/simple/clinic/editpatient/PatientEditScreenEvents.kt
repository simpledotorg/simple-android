package org.simple.clinic.editpatient

import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingEditPatientEntry
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.widgets.UiEvent

data class PatientEditScreenCreated(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?
) : UiEvent {
  companion object {
    fun fromPatientData(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?
    ): PatientEditScreenCreated {
      return PatientEditScreenCreated(
          patient,
          address,
          phoneNumber
      )
    }
  }
}

data class PatientEditPatientNameTextChanged(val name: String) : UiEvent {
  override val analyticsName = "Edit Patient Entry:Patient Name Text Changed"
}

data class PatientEditPhoneNumberTextChanged(val phoneNumber: String) : UiEvent {
  override val analyticsName = "Edit Patient Entry:Phone Number Text Changed"
}

data class PatientEditGenderChanged(val gender: Gender) : UiEvent {
  override val analyticsName = "Edit Patient Entry:Changed Gender"
}

data class PatientEditColonyOrVillageChanged(val colonyOrVillage: String): UiEvent {
  override val analyticsName = "Edit Patient Entry:Colony Or Village Text Changed"
}

data class PatientEditDistrictTextChanged(val district: String): UiEvent {
  override val analyticsName = "Edit Patient Entry:District Text Changed"
}

data class PatientEditStateTextChanged(val state: String): UiEvent {
  override val analyticsName = "Edit Patient Entry:State Text Changed"
}

data class OngoingEditPatientEntryChanged(val ongoingEditPatientEntry: OngoingEditPatientEntry): UiEvent

class PatientEditSaveClicked: UiEvent {
  override val analyticsName = "Edit Patient Entry:Save Clicked"
}

data class PatientEditDateOfBirthFocusChanged(val hasFocus: Boolean): UiEvent {
  override val analyticsName = "Edit Patient Entry:Focused on DOB Text Field"
}

data class PatientEditDateOfBirthTextChanged(val dateOfBirth: String): UiEvent {
  override val analyticsName = "Edit Patient Entry:DOB Text Changed"
}

data class PatientEditAgeTextChanged(val age: String): UiEvent {
  override val analyticsName = "Edit Patient Entry:Age Text Changed"
}

class PatientEditBackClicked: UiEvent {
  override val analyticsName = "Edit Patient Entry:Back Clicked"
}
