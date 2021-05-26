package org.simple.clinic.summary.medicalhistory

import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.widgets.UiEvent

sealed class MedicalHistorySummaryEvent : UiEvent

data class SummaryMedicalHistoryAnswerToggled(
    val question: MedicalHistoryQuestion,
    val answer: Answer
) : MedicalHistorySummaryEvent() {
  override val analyticsName = "Patient Summary:Answer for $question set to $answer"
}

data class MedicalHistoryLoaded(val medicalHistory: MedicalHistory) : MedicalHistorySummaryEvent()

data class CurrentFacilityLoaded(val facility: Facility) : MedicalHistorySummaryEvent()
