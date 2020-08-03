package org.simple.clinic.search

sealed class PatientSearchEffect

data class ReportValidationErrorsToAnalytics(val errors: Set<PatientSearchValidationError>) : PatientSearchEffect()
