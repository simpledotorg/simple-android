package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class BloodSugarSummaryViewEvent : UiEvent

data class BloodSugarSummaryFetched(val measurements: List<BloodSugarMeasurement>) : BloodSugarSummaryViewEvent()

data object NewBloodSugarClicked : BloodSugarSummaryViewEvent()

data class BloodSugarCountFetched(val count: Int) : BloodSugarSummaryViewEvent()

data object SeeAllClicked : BloodSugarSummaryViewEvent() {
  override val analyticsName: String = "Patient Summary:Blood Sugar See All Clicked"
}

data class BloodSugarClicked(val id: UUID, val measurementType: BloodSugarMeasurementType) : BloodSugarSummaryViewEvent() {
  override val analyticsName: String = "Patient Summary:Blood Sugar Clicked"
}
