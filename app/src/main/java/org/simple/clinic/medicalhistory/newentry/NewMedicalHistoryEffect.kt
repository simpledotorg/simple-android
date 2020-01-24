package org.simple.clinic.medicalhistory.newentry

import java.util.UUID

sealed class NewMedicalHistoryEffect

data class OpenPatientSummaryScreen(val patientUuid: UUID): NewMedicalHistoryEffect()
