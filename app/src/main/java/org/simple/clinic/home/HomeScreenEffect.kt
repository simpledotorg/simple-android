package org.simple.clinic.home

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier

sealed class HomeScreenEffect

object OpenFacilitySelection : HomeScreenEffect()

object LoadCurrentFacility : HomeScreenEffect()

data class LoadOverdueAppointmentCount(val facility: Facility) : HomeScreenEffect()

data class SearchPatientByIdentifier(val identifier: Identifier) : HomeScreenEffect()

data class OpenShortCodeSearchScreen(val shortCode: String) : HomeScreenEffect()
