package org.simple.clinic.newentry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.PersonalDetails
import org.simple.clinic.util.Optional

@Parcelize
data class PatientEntryModel(
    val patientEntry: OngoingNewPatientEntry? = null,
    val isSelectingGenderForTheFirstTime: Boolean = true
) : Parcelable {
  companion object {
    val DEFAULT = PatientEntryModel()
  }

  fun patientEntryFetched(patientEntry: OngoingNewPatientEntry): PatientEntryModel =
      copy(patientEntry = patientEntry)

  // TODO(rj): 2019-10-03 Replace this with Arrow's lens or flatten the model
  fun withGender(gender: Optional<Gender>): PatientEntryModel {
    return copy(
        patientEntry = patientEntry?.copy(
            personalDetails = patientEntry.personalDetails?.copy(
                gender = gender.toNullable()
            )
        )
    )
  }

  fun withAge(age: String): PatientEntryModel {
    val personalDetails = patientEntry?.personalDetails ?: PersonalDetails("", null, null, null)

    return copy(
        patientEntry = patientEntry?.copy(
            personalDetails = personalDetails.copy(age = age)
        )
    )
  }

  fun withDateOfBirth(dateOfBirth: String): PatientEntryModel {
    val personalDetails = patientEntry?.personalDetails ?: PersonalDetails("", null, null, null)

    return copy(
        patientEntry = patientEntry?.copy(
            personalDetails = personalDetails.copy(dateOfBirth = dateOfBirth)
        )
    )
  }
}
