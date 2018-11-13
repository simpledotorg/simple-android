package org.simple.clinic.editpatient

import org.simple.clinic.patient.Gender
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class PatientEditScreenCreated(val patientUuid: UUID) : UiEvent

data class PatientEditPatientNameTextChanged(val name: String) : UiEvent {
  override val analyticsName = "Patient Edit:Patient Name Text Changed"
}

data class PatientEditPhoneNumberTextChanged(val phoneNumber: String) : UiEvent {
  override val analyticsName = "Patient Edit:Phone Number Text Changed"
}

data class PatientEditGenderChanged(val gender: Gender) : UiEvent {
  override val analyticsName = "Patient Edit:Changed Gender"
}

data class PatientEditColonyOrVillageChanged(val colonyOrVillage: String): UiEvent {
  override val analyticsName = "Patient Edit:Colony Or Village Text Changed"
}

data class PatientEditDistrictTextChanged(val district: String): UiEvent {
  override val analyticsName = "Patient Edit:District Text Changed"
}

data class PatientEditStateTextChanged(val state: String): UiEvent {
  override val analyticsName = "Patient Edit:State Text Changed"
}

data class OngoingEditPatientEntryChanged(val ongoingEditPatientEntry: OngoingEditPatientEntry): UiEvent

class PatientEditSaveClicked: UiEvent {
  override val analyticsName = "Patient Edit:Save Clicked"
}
