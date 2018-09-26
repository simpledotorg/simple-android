package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.widgets.UiEvent

class SaveMedicalHistoryClicked : UiEvent {
  override val analyticsName = "New Medical History:Save Clicked"
}

data class NewMedicalHistoryAnswerToggled(val question: MedicalHistoryQuestion, val selected: Boolean) : UiEvent {
  override val analyticsName = "New Medical History:Answer for $question set to $selected"
}
