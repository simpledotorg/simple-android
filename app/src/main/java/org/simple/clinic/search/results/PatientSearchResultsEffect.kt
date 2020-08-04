package org.simple.clinic.search.results

import java.util.UUID

sealed class PatientSearchResultsEffect

data class OpenPatientSummary(val patientUuid: UUID) : PatientSearchResultsEffect()
