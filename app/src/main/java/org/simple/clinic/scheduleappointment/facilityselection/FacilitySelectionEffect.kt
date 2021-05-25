package org.simple.clinic.scheduleappointment.facilityselection

import org.simple.clinic.facility.Facility

sealed class FacilitySelectionEffect

data class ForwardSelectedFacility(val facility: Facility) : FacilitySelectionEffect()
