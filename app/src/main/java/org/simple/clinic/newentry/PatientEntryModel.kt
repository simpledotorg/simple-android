package org.simple.clinic.newentry

import org.simple.clinic.patient.OngoingNewPatientEntry

data class PatientEntryModel(
    val patientEntry: OngoingNewPatientEntry? = null
) {
  companion object {
    val DEFAULT = PatientEntryModel()
  }

  fun patientEntryFetched(patientEntry: OngoingNewPatientEntry): PatientEntryModel =
      copy(patientEntry = patientEntry)
}
