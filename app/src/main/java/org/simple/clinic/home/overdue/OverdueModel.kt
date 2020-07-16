package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class OverdueModel(
    private val facility: Facility?,
    private val overdueAppointments: List<OverdueAppointment>?
) : Parcelable {

  companion object {
    fun create(): OverdueModel {
      return OverdueModel(
          facility = null,
          overdueAppointments = null
      )
    }
  }

  val hasLoadedCurrentFacility: Boolean
    get() = facility != null

  fun currentFacilityLoaded(facility: Facility): OverdueModel {
    return copy(facility = facility)
  }

  fun overdueAppointmentsLoaded(appointments: List<OverdueAppointment>): OverdueModel {
    return copy(overdueAppointments = appointments)
  }
}
