package org.simple.clinic.newentry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.util.Optional

@Parcelize
data class PatientEntryModel(
    val patientEntry: OngoingNewPatientEntry = OngoingNewPatientEntry(),
    val isSelectingGenderForTheFirstTime: Boolean = true
) : Parcelable {
  companion object {
    val DEFAULT = PatientEntryModel()
  }

  fun patientEntryFetched(patientEntry: OngoingNewPatientEntry): PatientEntryModel =
      copy(patientEntry = patientEntry)

  fun selectedGender(): PatientEntryModel =
      copy(isSelectingGenderForTheFirstTime = false)

  fun withGender(gender: Optional<Gender>): PatientEntryModel =
      copy(patientEntry = patientEntry.withGender(gender))

  fun withAge(age: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withAge(age))

  fun withDateOfBirth(dateOfBirth: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withDateOfBirth(dateOfBirth))

  fun withFullName(fullName: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withFullName(fullName))

  fun withPhoneNumber(phoneNumber: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withPhoneNumber(phoneNumber))

  fun withColonyOrVillage(colonyOrVillage: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withColonyOrVillage(colonyOrVillage))

  fun withDistrict(district: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withDistrict(district))

  fun withState(state: String): PatientEntryModel =
      copy(patientEntry = patientEntry.withState(state))
}
