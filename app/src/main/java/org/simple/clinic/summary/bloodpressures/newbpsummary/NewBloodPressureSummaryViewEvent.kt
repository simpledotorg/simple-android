package org.simple.clinic.summary.bloodpressures.newbpsummary

import org.simple.clinic.bp.BloodPressureMeasurement

sealed class NewBloodPressureSummaryViewEvent

data class BloodPressuresLoaded(val measurements: List<BloodPressureMeasurement>) : NewBloodPressureSummaryViewEvent()

data class BloodPressuresCountLoaded(val count: Int) : NewBloodPressureSummaryViewEvent()

object NewBloodPressureClicked : NewBloodPressureSummaryViewEvent()
