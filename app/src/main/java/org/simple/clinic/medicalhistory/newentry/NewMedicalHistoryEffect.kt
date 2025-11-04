package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import java.util.UUID

sealed class NewMedicalHistoryEffect

data class RegisterPatient(val ongoingMedicalHistoryEntry: OngoingMedicalHistoryEntry) : NewMedicalHistoryEffect()

data object LoadOngoingPatientEntry : NewMedicalHistoryEffect()

data object LoadCurrentFacility : NewMedicalHistoryEffect()

data class TriggerSync(val registeredPatientUuid: UUID) : NewMedicalHistoryEffect()

sealed class NewMedicalHistoryViewEffect : NewMedicalHistoryEffect()

data class OpenPatientSummaryScreen(val patientUuid: UUID) : NewMedicalHistoryViewEffect()

data object ShowOngoingHypertensionTreatmentError : NewMedicalHistoryViewEffect()

data object ShowOngoingDiabetesTreatmentErrorDialog : NewMedicalHistoryViewEffect()

data object GoBack : NewMedicalHistoryViewEffect()
