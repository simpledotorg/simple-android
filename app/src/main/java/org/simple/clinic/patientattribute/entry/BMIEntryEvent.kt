package org.simple.clinic.patientattribute.entry

import org.simple.clinic.widgets.UiEvent

sealed class BMIEntryEvent : UiEvent

data object SaveClicked : BMIEntryEvent() {
  override val analyticsName = "BMI Entry:Save Clicked"
}

