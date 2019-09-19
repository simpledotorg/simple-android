package org.simple.clinic.editpatient

import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingEditPatientEntry
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class PatientEditScreenCreated(
    open val patientUuid: UUID // TODO(rj) 19/Sep/19 - Remove the `patientUuid` property
) : UiEvent {
  companion object {
    fun fromPatientUuid(uuid: UUID): PatientEditScreenCreated {
      return PatientEditScreenCreatedWithUuid(uuid)
    }

    fun fromPatientData(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?
    ): PatientEditScreenCreated {
      return PatientEditScreenCreatedWithData(
          patient.uuid,
          patient,
          address,
          phoneNumber
      )
    }
  }

  @Deprecated("""
    We are removing the necessity to query patient information from their UUID,
    instead we'll pass all required information from the previous screen
    """)
  data class PatientEditScreenCreatedWithUuid(
      override val patientUuid: UUID
  ) : PatientEditScreenCreated(patientUuid)

  data class PatientEditScreenCreatedWithData(
      override val patientUuid: UUID,
      val patient: Patient,
      val address: PatientAddress,
      val phoneNumber: PatientPhoneNumber?
  ) : PatientEditScreenCreated(patientUuid)
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
