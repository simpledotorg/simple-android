package org.simple.clinic.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class HomeScreenModel(
    val facility: Facility?,
    val overdueAppointmentCount: Int?
) : Parcelable {

  companion object {
    fun create() = HomeScreenModel(
        facility = null,
        overdueAppointmentCount = null
    )
  }

  val hasFacility: Boolean
    get() = facility != null

  val hasOverdueAppointmentCount: Boolean
    get() = overdueAppointmentCount != null

  val hasAtLeastOneOverdueAppointment: Boolean
    get() = overdueAppointmentCount!! > 0

  fun facilityLoaded(facility: Facility): HomeScreenModel {
    return copy(facility = facility)
  }

  fun overdueAppointmentCountLoaded(overdueAppointmentCount: Int): HomeScreenModel {
    return copy(overdueAppointmentCount = overdueAppointmentCount)
  }
}
