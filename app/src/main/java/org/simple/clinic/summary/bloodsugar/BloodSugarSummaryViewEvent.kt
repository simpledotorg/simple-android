package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurement

sealed class BloodSugarSummaryViewEvent

data class BloodSugarSummaryFetched(val measurements: List<BloodSugarMeasurement>) : BloodSugarSummaryViewEvent()

object NewBloodSugarClicked : BloodSugarSummaryViewEvent()
