package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import java.util.UUID

sealed class NewMedicalHistoryEffect

data class OpenPatientSummaryScreen(val patientUuid: UUID) : NewMedicalHistoryEffect()

data class RegisterPatient(val ongoingMedicalHistoryEntry: OngoingMedicalHistoryEntry) : NewMedicalHistoryEffect()
