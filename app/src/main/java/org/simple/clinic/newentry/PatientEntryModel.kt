package org.simple.clinic.newentry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.Address
import org.simple.clinic.patient.OngoingNewPatientEntry.PersonalDetails
import org.simple.clinic.patient.OngoingNewPatientEntry.PhoneNumber
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

  fun withGender(gender: Optional<Gender>): PatientEntryModel =
      copy(patientEntry = patientEntry.copy(personalDetails = patientEntry.personalDetails?.copy(gender = gender.toNullable())))

  fun withAge(age: String): PatientEntryModel {
    val personalDetails = personalDetailsOrBlank()
    return copy(patientEntry = patientEntry.copy(personalDetails = personalDetails.copy(age = if (age.isBlank()) null else age)))
  }

  fun withDateOfBirth(dateOfBirth: String): PatientEntryModel {
    val personalDetails = personalDetailsOrBlank()
    return copy(patientEntry = patientEntry.copy(personalDetails = personalDetails.copy(dateOfBirth = if (dateOfBirth.isBlank()) null else dateOfBirth)))
  }

  fun withFullName(fullName: String): PatientEntryModel {
    val personalDetails = personalDetailsOrBlank()
    return copy(patientEntry = patientEntry.copy(personalDetails = personalDetails.copy(fullName = fullName)))
  }

  fun withPhoneNumber(phoneNumber: String): PatientEntryModel =
      copy(patientEntry = patientEntry.copy(phoneNumber = if (phoneNumber.isNotBlank()) PhoneNumber(phoneNumber) else null))

  fun withColonyOrVillage(colonyOrVillage: String): PatientEntryModel {
    val address = addressOrBlank()
    return copy(patientEntry = patientEntry.copy(address = address.copy(colonyOrVillage = colonyOrVillage)))
  }

  fun withDistrict(district: String): PatientEntryModel {
    val address = addressOrBlank()
    return copy(patientEntry = patientEntry.copy(address = address.copy(district = district)))
  }

  fun withState(state: String): PatientEntryModel {
    val address = addressOrBlank()
    return copy(patientEntry = patientEntry.copy(address = address.copy(state = state)))
  }

  fun selectedGender(): PatientEntryModel =
      copy(isSelectingGenderForTheFirstTime = false)

  private fun personalDetailsOrBlank(): PersonalDetails =
      patientEntry.personalDetails ?: PersonalDetails.BLANK

  private fun addressOrBlank(): Address =
      patientEntry.address ?: Address.BLANK
}
