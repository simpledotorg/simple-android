package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility

sealed class InstantSearchEffect

object LoadCurrentFacility : InstantSearchEffect()

data class LoadAllPatients(val facility: Facility) : InstantSearchEffect()
