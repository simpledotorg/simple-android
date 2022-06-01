package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class OverdueModel(
    val facility: Facility?,
    val pendingAppointments: List<OverdueAppointment>?,
    val agreedToVisitAppointments: List<OverdueAppointment>?,
    val remindToCallLaterAppointments: List<OverdueAppointment>?,
    val removedFromOverdueAppointments: List<OverdueAppointment>?,
    val moreThanAnYearOverdueAppointments: List<OverdueAppointment>?
) : Parcelable {

  companion object {
    fun create(): OverdueModel {
      return OverdueModel(
          facility = null,
          pendingAppointments = null,
          agreedToVisitAppointments = null,
          remindToCallLaterAppointments = null,
          removedFromOverdueAppointments = null,
          moreThanAnYearOverdueAppointments = null
      )
    }
  }

  val isDiabetesManagementEnabled: Boolean
    get() = facility!!.config.diabetesManagementEnabled

  val hasLoadedCurrentFacility: Boolean
    get() = facility != null

  val hasLoadedOverdueAppointments: Boolean
    get() = pendingAppointments != null &&
        agreedToVisitAppointments != null &&
        remindToCallLaterAppointments != null &&
        removedFromOverdueAppointments != null &&
        moreThanAnYearOverdueAppointments != null

  fun currentFacilityLoaded(facility: Facility): OverdueModel {
    return copy(facility = facility)
  }

  fun overdueAppointmentsLoaded(
      pendingAppointments: List<OverdueAppointment>,
      agreedToVisitAppointments: List<OverdueAppointment>,
      remindToCallLaterAppointments: List<OverdueAppointment>,
      removedFromOverdueAppointments: List<OverdueAppointment>,
      moreThanAnYearOverdueAppointments: List<OverdueAppointment>
  ): OverdueModel {
    return copy(
        pendingAppointments = pendingAppointments,
        agreedToVisitAppointments = agreedToVisitAppointments,
        remindToCallLaterAppointments = remindToCallLaterAppointments,
        removedFromOverdueAppointments = removedFromOverdueAppointments,
        moreThanAnYearOverdueAppointments = moreThanAnYearOverdueAppointments
    )
  }
}
