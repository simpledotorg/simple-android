package org.simple.clinic.summary.bloodpressures.newbpsummary

import java.util.UUID

sealed class NewBloodPressureSummaryViewEffect

data class LoadBloodPressures(val patientUuid: UUID, val numberOfBpsToDisplay: Int) : NewBloodPressureSummaryViewEffect()

data class LoadBloodPressuresCount(val patientUuid: UUID) : NewBloodPressureSummaryViewEffect()
