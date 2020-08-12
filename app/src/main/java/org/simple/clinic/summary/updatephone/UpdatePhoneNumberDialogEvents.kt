package org.simple.clinic.summary.updatephone

import org.simple.clinic.widgets.UiEvent

object UpdatePhoneNumberCancelClicked : UiEvent {
  override val analyticsName = "Patient Summary:Update Phone Number:Cancel Clicked"
}

data class UpdatePhoneNumberSaveClicked(val number: String) : UiEvent {
  override val analyticsName = "Patient Summary:Update Phone Number:Save Clicked"
}
