package org.simple.clinic.searchresultsview

import org.simple.clinic.patient.PatientSearchCriteria

sealed class SearchResultsEffect

data class SearchWithCriteria(val searchCriteria: PatientSearchCriteria) : SearchResultsEffect()
