package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import java.util.UUID

sealed class NewMedicalHistoryEffect

data class RegisterPatient(val ongoingMedicalHistoryEntry: OngoingMedicalHistoryEntry) : NewMedicalHistoryEffect()

object LoadOngoingPatientEntry : NewMedicalHistoryEffect()

object LoadCurrentFacility : NewMedicalHistoryEffect()

data class TriggerSync(val registeredPatientUuid: UUID) : NewMedicalHistoryEffect()

object ShowOngoingHypertensionTreatmentError : NewMedicalHistoryEffect()

object ShowDiagnosisRequiredError : NewMedicalHistoryEffect()

object ShowHypertensionDiagnosisRequiredError : NewMedicalHistoryEffect()

sealed class NewMedicalHistoryViewEffect : NewMedicalHistoryEffect()

data class OpenPatientSummaryScreen(val patientUuid: UUID) : NewMedicalHistoryViewEffect()
