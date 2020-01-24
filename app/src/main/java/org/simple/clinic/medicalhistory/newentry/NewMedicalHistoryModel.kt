package org.simple.clinic.medicalhistory.newentry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.patient.OngoingNewPatientEntry

@Parcelize
data class NewMedicalHistoryModel(
    val ongoingPatientEntry: OngoingNewPatientEntry?,
    val ongoingMedicalHistoryEntry: OngoingMedicalHistoryEntry
) : Parcelable {

  val hasLoadedPatientEntry: Boolean
    get() = ongoingPatientEntry != null

  companion object {
    fun default(): NewMedicalHistoryModel = NewMedicalHistoryModel(null, OngoingMedicalHistoryEntry())
  }

  fun answerChanged(question: MedicalHistoryQuestion, answer: Answer): NewMedicalHistoryModel {
    return copy(ongoingMedicalHistoryEntry = ongoingMedicalHistoryEntry.answerChanged(question, answer))
  }

  fun ongoingPatientEntryLoaded(entry: OngoingNewPatientEntry): NewMedicalHistoryModel {
    return copy(ongoingPatientEntry = entry)
  }
}
