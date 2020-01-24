package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.widgets.UiEvent

sealed class NewMedicalHistoryEvent : UiEvent

class SaveMedicalHistoryClicked : NewMedicalHistoryEvent() {
  override val analyticsName = "New Medical History:Save Clicked"
}
