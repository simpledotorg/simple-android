package org.simple.clinic.search

import org.simple.clinic.patient.PatientSearchCriteria
import java.util.UUID

sealed class PatientSearchEffect

data class ReportValidationErrorsToAnalytics(val errors: Set<PatientSearchValidationError>) : PatientSearchEffect()

data class OpenSearchResults(val searchCriteria: PatientSearchCriteria): PatientSearchEffect()

data class OpenPatientSummary(val patientId: UUID): PatientSearchEffect()
