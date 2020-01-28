package org.simple.clinic.summary.bloodpressures.newbpsummary

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.widgets.UiEvent

sealed class NewBloodPressureSummaryViewEvent : UiEvent

data class BloodPressuresLoaded(val measurements: List<BloodPressureMeasurement>) : NewBloodPressureSummaryViewEvent()

data class BloodPressuresCountLoaded(val count: Int) : NewBloodPressureSummaryViewEvent()

object AddNewBloodPressureClicked : NewBloodPressureSummaryViewEvent() {
  override val analyticsName = "Patient Summary:Add New BP Clicked"
}

data class BloodPressureClicked(val measurement: BloodPressureMeasurement) : NewBloodPressureSummaryViewEvent() {
  override val analyticsName = "Patient Summary:BP Clicked for editing"
}

object SeeAllClicked : NewBloodPressureSummaryViewEvent() {
  override val analyticsName = "Patient Summary:See All Clicked"
}
