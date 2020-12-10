package org.simple.clinic.home

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

sealed class HomeScreenEffect

object OpenFacilitySelection : HomeScreenEffect()

object LoadCurrentFacility : HomeScreenEffect()

data class LoadOverdueAppointmentCount(val facility: Facility) : HomeScreenEffect()

data class OpenShortCodeSearchScreen(val shortCode: String) : HomeScreenEffect()

data class OpenPatientSearchScreen(val additionalIdentifier: Identifier?) : HomeScreenEffect()

data class OpenPatientSummary(val patientId: UUID) : HomeScreenEffect()
