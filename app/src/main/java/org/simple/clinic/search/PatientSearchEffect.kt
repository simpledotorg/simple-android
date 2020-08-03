package org.simple.clinic.search

import org.simple.clinic.patient.PatientSearchCriteria

sealed class PatientSearchEffect

data class ReportValidationErrorsToAnalytics(val errors: Set<PatientSearchValidationError>) : PatientSearchEffect()

data class OpenSearchResults(val searchCriteria: PatientSearchCriteria): PatientSearchEffect()
