package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.widgets.UiEvent

sealed class BloodSugarSummaryViewEvent : UiEvent

data class BloodSugarSummaryFetched(val measurements: List<BloodSugarMeasurement>) : BloodSugarSummaryViewEvent()

object NewBloodSugarClicked : BloodSugarSummaryViewEvent()

data class BloodSugarCountFetched(val count: Int) : BloodSugarSummaryViewEvent()

object SeeAllClicked : BloodSugarSummaryViewEvent() {
  override val analyticsName: String = "Patient Summary:Blood Sugar See All Clicked"
}

data class BloodSugarClicked(val bloodSugarMeasurement: BloodSugarMeasurement) : BloodSugarSummaryViewEvent() {
  override val analyticsName: String = "Patient Summary:Blood Sugar Clicked"
}
