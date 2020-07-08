package org.simple.clinic.scheduleappointment.facilityselection

import org.simple.clinic.facility.Facility

interface FacilitySelectionUi : FacilitySelectionUiActions {
  fun sendSelectedFacility(selectedFacility: Facility)
}
