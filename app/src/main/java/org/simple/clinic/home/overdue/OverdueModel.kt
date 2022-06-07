package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.home.overdue.PendingListState.SEE_LESS

@Parcelize
data class OverdueModel(
    val facility: Facility?,
    val overdueAppointmentSections: OverdueAppointmentSections?,
    val pendingListState: PendingListState
) : Parcelable {

  companion object {
    fun create(): OverdueModel {
      return OverdueModel(
          facility = null,
          overdueAppointmentSections = null,
          pendingListState = SEE_LESS
      )
    }
  }

  val isDiabetesManagementEnabled: Boolean
    get() = facility!!.config.diabetesManagementEnabled

  val hasLoadedCurrentFacility: Boolean
    get() = facility != null

  val hasLoadedOverdueAppointments: Boolean
    get() = overdueAppointmentSections != null

  val overdueCount: Int
    get() = overdueAppointmentSections!!.overdueCount

  fun currentFacilityLoaded(facility: Facility): OverdueModel {
    return copy(facility = facility)
  }

  fun overdueAppointmentsLoaded(
      overdueAppointmentSections: OverdueAppointmentSections
  ): OverdueModel {
    return copy(overdueAppointmentSections = overdueAppointmentSections)
  }

  fun pendingListStateChanged(state: PendingListState): OverdueModel {
    return copy(pendingListState = state)
  }
}
