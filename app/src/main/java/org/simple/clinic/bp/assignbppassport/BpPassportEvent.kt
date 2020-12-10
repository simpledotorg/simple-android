package org.simple.clinic.bp.assignbppassport

import org.simple.clinic.facility.Facility

sealed class BpPassportEvent

object NewOngoingPatientEntrySaved : BpPassportEvent()

data class CurrentFacilityRetrieved(val facility: Facility): BpPassportEvent()

object RegisterNewPatientClicked : BpPassportEvent()
