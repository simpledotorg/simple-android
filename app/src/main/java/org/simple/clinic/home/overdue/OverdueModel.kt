package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class OverdueModel(
    val facility: Facility?,
    val overdueAppointmentOlds: List<OverdueAppointment_Old>?
) : Parcelable {

  companion object {
    fun create(): OverdueModel {
      return OverdueModel(
          facility = null,
          overdueAppointmentOlds = null
      )
    }
  }

  val isDiabetesManagementEnabled: Boolean
    get() = facility!!.config.diabetesManagementEnabled

  val hasLoadedOverdueAppointments: Boolean
    get() = overdueAppointmentOlds != null

  val hasLoadedCurrentFacility: Boolean
    get() = facility != null

  fun currentFacilityLoaded(facility: Facility): OverdueModel {
    return copy(facility = facility)
  }

  fun overdueAppointmentsLoaded(appointmentOlds: List<OverdueAppointment_Old>): OverdueModel {
    return copy(overdueAppointmentOlds = appointmentOlds)
  }
}
