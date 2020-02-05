package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

sealed class BloodPressureSummaryViewEvent : UiEvent

data class BloodPressuresLoaded(val measurements: List<BloodPressureMeasurement>) : BloodPressureSummaryViewEvent()

data class BloodPressuresCountLoaded(val count: Int) : BloodPressureSummaryViewEvent()

data class CurrentFacilityLoaded(val facility: Facility) : BloodPressureSummaryViewEvent()

object AddNewBloodPressureClicked : BloodPressureSummaryViewEvent() {
  override val analyticsName = "Patient Summary:Add New BP Clicked"
}

data class BloodPressureClicked(val measurement: BloodPressureMeasurement) : BloodPressureSummaryViewEvent() {
  override val analyticsName = "Patient Summary:BP Clicked for editing"
}

object SeeAllClicked : BloodPressureSummaryViewEvent() {
  override val analyticsName = "Patient Summary:See All Clicked"
}
