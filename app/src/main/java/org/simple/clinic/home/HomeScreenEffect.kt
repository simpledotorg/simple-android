package org.simple.clinic.home

import org.simple.clinic.facility.Facility

sealed class HomeScreenEffect

object OpenFacilitySelection : HomeScreenEffect()

object LoadCurrentFacility : HomeScreenEffect()

data class LoadOverdueAppointmentCount(val facility: Facility) : HomeScreenEffect()
