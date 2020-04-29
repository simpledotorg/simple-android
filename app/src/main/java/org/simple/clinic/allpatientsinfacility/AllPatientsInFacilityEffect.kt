package org.simple.clinic.allpatientsinfacility

import org.simple.clinic.facility.Facility

sealed class AllPatientsInFacilityEffect

object FetchFacilityEffect : AllPatientsInFacilityEffect()

data class FetchPatientsEffect(
    val facility: Facility
) : AllPatientsInFacilityEffect()
