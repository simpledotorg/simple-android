package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.widgets.UiEvent

sealed class NewMedicalHistoryEvent : UiEvent

class SaveMedicalHistoryClicked : NewMedicalHistoryEvent() {
  override val analyticsName = "New Medical History:Save Clicked"
}

data class NewMedicalHistoryAnswerToggled(val question: MedicalHistoryQuestion, val answer: Answer) : NewMedicalHistoryEvent() {
  // TODO(vs): 2020-01-24 Fix .toString() issues with the answer property since it is not an enum
  override val analyticsName = "New Medical History:Answer for $question set to $answer"
}
