package org.simple.clinic.editpatient_old

import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.widgets.UiEvent

data class ScreenCreated(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?
) : UiEvent {
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
