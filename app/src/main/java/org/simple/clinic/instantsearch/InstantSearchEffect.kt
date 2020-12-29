package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchCriteria

sealed class InstantSearchEffect

object LoadCurrentFacility : InstantSearchEffect()

data class LoadAllPatients(val facility: Facility) : InstantSearchEffect()

data class SearchWithCriteria(val criteria: PatientSearchCriteria, val facility: Facility) : InstantSearchEffect()
