package org.simple.clinic.patientattribute.entry

import org.simple.clinic.widgets.UiEvent

sealed class BMIEntryEvent : UiEvent

data class HeightChanged(val height: String) : BMIEntryEvent() {
  override val analyticsName = "BMI Entry:Height Changed"
}

data class WeightChanged(val weight: String) : BMIEntryEvent() {
  override val analyticsName = "BMI Entry:Weight Changed"
}

data object WeightBackspaceClicked : BMIEntryEvent()

data object SaveClicked : BMIEntryEvent() {
  override val analyticsName = "BMI Entry:Save Clicked"
}

data object BackPressed : BMIEntryEvent() {
  override val analyticsName = "BMI Entry::Back Pressed"
}

data object BMISaved : BMIEntryEvent()
