package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class OverdueModel(
    val facility: Facility?,
    val overdueAppointments: List<OverdueAppointment>?
) : Parcelable {

  companion object {
    fun create(): OverdueModel {
      return OverdueModel(
          facility = null,
          overdueAppointments = null
      )
    }
  }

  val isDiabetesManagementEnabled: Boolean
    get() = facility!!.config.diabetesManagementEnabled

  val hasLoadedOverdueAppointments: Boolean
    get() = overdueAppointments != null

  val hasLoadedCurrentFacility: Boolean
    get() = facility != null

  fun currentFacilityLoaded(facility: Facility): OverdueModel {
    return copy(facility = facility)
  }

  fun overdueAppointmentsLoaded(appointments: List<OverdueAppointment>): OverdueModel {
    return copy(overdueAppointments = appointments)
  }
}
