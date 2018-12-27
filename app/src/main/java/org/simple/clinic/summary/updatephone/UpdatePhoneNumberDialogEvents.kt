package org.simple.clinic.summary.updatephone

import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.widgets.UiEvent

data class UpdatePhoneNumberDialogCreated(val patientUuid: PatientUuid) : UiEvent {
  override val analyticsName = "Patient Summary:Update Phone Number:Dialog Created"
}

object UpdatePhoneNumberCancelClicked : UiEvent {
  override val analyticsName = "Patient Summary:Update Phone Number:Cancel Clicked"
}

data class UpdatePhoneNumberSaveClicked(val number: String) : UiEvent {
  override val analyticsName = "Patient Summary:Update Phone Number:Save Clicked"
}
