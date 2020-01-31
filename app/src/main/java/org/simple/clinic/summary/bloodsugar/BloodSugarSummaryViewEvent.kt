package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.widgets.UiEvent

sealed class BloodSugarSummaryViewEvent : UiEvent

data class BloodSugarSummaryFetched(val measurements: List<BloodSugarMeasurement>) : BloodSugarSummaryViewEvent()

object NewBloodSugarClicked : BloodSugarSummaryViewEvent()

data class BloodSugarCountFetched(val count: Int) : BloodSugarSummaryViewEvent()
