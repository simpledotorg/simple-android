package org.simple.clinic.summary.medicalhistory

import java.util.UUID

sealed class MedicalHistorySummaryEffect

data class LoadMedicalHistory(val patientUUID: UUID): MedicalHistorySummaryEffect()
