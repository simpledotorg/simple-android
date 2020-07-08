package org.simple.clinic.scheduleappointment.facilityselection

import org.simple.clinic.facility.Facility

interface FacilitySelectionUi {
  fun sendSelectedFacility(selectedFacility: Facility)
}
