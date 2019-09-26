package org.simple.clinic.editpatient

import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.threeten.bp.format.DateTimeFormatter

data class EditPatientModel(
    val savedEntry: OngoingEditPatientEntry,
    val ongoingEntry: OngoingEditPatientEntry
) {
  companion object {
    fun from(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?,
        dateOfBirthFormatter: DateTimeFormatter
    ): EditPatientModel {
      val savedEntry = OngoingEditPatientEntry.from(patient, address, phoneNumber, dateOfBirthFormatter)
      val ongoingEntry = savedEntry.copy()
      return EditPatientModel(savedEntry, ongoingEntry)
    }
  }

  fun updateName(name: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateName(name))

  fun updateGender(gender: Gender): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateGender(gender))

  fun updatePhoneNumber(phoneNumber: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updatePhoneNumber(phoneNumber))

  fun updateColonyOrVillage(colonyOrVillage: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateColonyOrVillage(colonyOrVillage))

  fun updateDistrict(district: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateDistrict(district))

  fun updateState(state: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateState(state))

  fun updateAge(age: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateAge(age))

  fun updateDateOfBirth(dateOfBirth: String): EditPatientModel =
      copy(ongoingEntry = ongoingEntry.updateDateOfBirth(dateOfBirth))
}
