package org.simple.clinic.summary.addphone

import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.widgets.UiEvent

data class AddPhoneNumberDialogCreated(val patientUuid: PatientUuid) : UiEvent {
  override val analyticsName = "Patient Summary:Add Phone Number:Dialog Created"
}

data class AddPhoneNumberSaveClicked(val number: String) : UiEvent {
  override val analyticsName = "Patient Summary:Add Phone Number:Save Clicked"
}
