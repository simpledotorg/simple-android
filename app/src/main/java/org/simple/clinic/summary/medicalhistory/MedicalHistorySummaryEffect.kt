package org.simple.clinic.summary.medicalhistory

import org.simple.clinic.medicalhistory.MedicalHistory
import java.util.UUID

sealed class MedicalHistorySummaryEffect

data class LoadMedicalHistory(val patientUUID: UUID) : MedicalHistorySummaryEffect()

object LoadCurrentFacility : MedicalHistorySummaryEffect()

data class SaveUpdatedMedicalHistory(val medicalHistory: MedicalHistory) : MedicalHistorySummaryEffect()
