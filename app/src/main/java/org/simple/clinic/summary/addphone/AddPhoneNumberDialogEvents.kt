package org.simple.clinic.summary.addphone

import org.simple.clinic.widgets.UiEvent

data class AddPhoneNumberSaveClicked(val number: String) : UiEvent {
  override val analyticsName = "Patient Summary:Add Phone Number:Save Clicked"
}
