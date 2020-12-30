package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchResult

sealed class InstantSearchEffect

object LoadCurrentFacility : InstantSearchEffect()

data class LoadAllPatients(val facility: Facility) : InstantSearchEffect()

data class SearchWithCriteria(val criteria: PatientSearchCriteria, val facility: Facility) : InstantSearchEffect()

data class ShowPatientSearchResults(val patients: List<PatientSearchResult>, val facility: Facility) : InstantSearchEffect()

data class ValidateSearchQuery(val searchQuery: String) : InstantSearchEffect()
