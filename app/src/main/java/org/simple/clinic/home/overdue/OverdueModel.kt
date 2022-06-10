package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.home.overdue.PendingListState.SEE_LESS

@Parcelize
data class OverdueModel(
    val facility: Facility?,
    @IgnoredOnParcel
    val overdueAppointmentSections: OverdueAppointmentSections? = null,
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

  val isOverdueAppointmentSectionsListEmpty: Boolean
    get() = overdueCount == 0

  val pendingHeaderExpanded: Boolean
    get() = overdueAppointmentSections?.isPendingHeaderExpanded == true

  val agreedToVisitHeaderExpanded: Boolean
    get() = overdueAppointmentSections?.isAgreedToVisitHeaderExpanded == true

  val remindToCallLaterHeaderExpanded: Boolean
    get() = overdueAppointmentSections?.isRemindToCallLaterHeaderExpanded == true

  val removedFromOverdueListHeaderExpanded: Boolean
    get() = overdueAppointmentSections?.isRemovedFromOverdueListHeaderExpanded == true

  val moreThanAnOneYearOverdueHeader: Boolean
    get() = overdueAppointmentSections?.isMoreThanAnOneYearOverdueHeader == true

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

  fun pendingChevronStateIsChanged(pendingChevronStateIsChanged: Boolean): OverdueModel {
    return copy(overdueAppointmentSections = overdueAppointmentSections?.pendingChevronStateIsChanged(pendingChevronStateIsChanged))
  }

  fun agreedToVisitChevronStateIsChanged(agreedToVisitChevronStateIsChanged: Boolean): OverdueModel {
    return copy(overdueAppointmentSections = overdueAppointmentSections?.agreedToVisitChevronStateIsChanged(agreedToVisitChevronStateIsChanged))
  }

  fun remindToCallChevronStateIsChanged(remindToCallChevronStateIsChanged: Boolean): OverdueModel {
    return copy(overdueAppointmentSections = overdueAppointmentSections?.remindToCallChevronStateIsChanged(remindToCallChevronStateIsChanged))
  }

  fun removedFromOverdueChevronStateIsChanged(removedFromOverdueChevronStateIsChanged: Boolean): OverdueModel {
    return copy(overdueAppointmentSections = overdueAppointmentSections?.removedFromOverdueChevronStateIsChanged(removedFromOverdueChevronStateIsChanged))
  }

  fun moreThanAYearChevronStateIsChanged(moreThanAYearChevronStateIsChanged: Boolean): OverdueModel {
    return copy(overdueAppointmentSections = overdueAppointmentSections?.moreThanAYearChevronStateIsChanged(moreThanAYearChevronStateIsChanged))
  }
}
