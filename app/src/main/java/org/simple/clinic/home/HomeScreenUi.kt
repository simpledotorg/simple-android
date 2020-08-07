package org.simple.clinic.home

interface HomeScreenUi {
  fun setFacility(facilityName: String)
  fun showOverdueAppointmentCount(count: Int)
  fun removeOverdueAppointmentCount()
}
