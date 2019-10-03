package org.simple.clinic.newentry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
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
  fun updateGender(gender: Optional<Gender>): PatientEntryModel {
    return copy(
        patientEntry = patientEntry?.copy(
            personalDetails = patientEntry.personalDetails?.copy(
                gender = gender.toNullable()
            )
        )
    )
  }
}
