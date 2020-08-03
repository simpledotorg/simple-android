package org.simple.clinic.summary.prescribeddrugs

import java.util.UUID

sealed class DrugSummaryEffect

data class LoadPrescribedDrugs(val patientUuid: UUID) : DrugSummaryEffect()
