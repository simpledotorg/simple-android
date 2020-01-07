package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.widgets.UiEvent

sealed class BloodPressureSummaryEvent : UiEvent

object NewBloodPressureClicked : BloodPressureSummaryEvent() {
  override val analyticsName = "Patient Summary:New BP Clicked"
}
