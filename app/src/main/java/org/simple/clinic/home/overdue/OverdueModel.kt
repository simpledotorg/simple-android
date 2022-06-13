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
    val overdueListSectionStates: OverdueListSectionStates
) : Parcelable {

  companion object {
    fun create(): OverdueModel {
      return OverdueModel(
          facility = null,
          overdueAppointmentSections = null,
          overdueListSectionStates = OverdueListSectionStates(
              pendingListState = SEE_LESS,
              isPendingHeaderExpanded = true,
              isAgreedToVisitHeaderExpanded = false,
              isRemindToCallLaterHeaderExpanded = false,
              isRemovedFromOverdueListHeaderExpanded = false,
              isMoreThanAnOneYearOverdueHeader = false
          )
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
    get() = overdueListSectionStates.isPendingHeaderExpanded

  val agreedToVisitHeaderExpanded: Boolean
    get() = overdueListSectionStates.isAgreedToVisitHeaderExpanded

  val remindToCallLaterHeaderExpanded: Boolean
    get() = overdueListSectionStates.isRemindToCallLaterHeaderExpanded

  val removedFromOverdueListHeaderExpanded: Boolean
    get() = overdueListSectionStates.isRemovedFromOverdueListHeaderExpanded

  val moreThanAnOneYearOverdueHeader: Boolean
    get() = overdueListSectionStates.isMoreThanAnOneYearOverdueHeader

  fun currentFacilityLoaded(facility: Facility): OverdueModel {
    return copy(facility = facility)
  }

  fun overdueAppointmentsLoaded(
      overdueAppointmentSections: OverdueAppointmentSections
  ): OverdueModel {
    return copy(overdueAppointmentSections = overdueAppointmentSections)
  }

  fun pendingListStateChanged(pendingListState: PendingListState): OverdueModel {
    return copy(overdueListSectionStates = overdueListSectionStates.pendingListStateChanged(pendingListState))
  }

  fun pendingChevronStateIsChanged(pendingChevronStateIsChanged: Boolean): OverdueModel {
    return copy(overdueListSectionStates = overdueListSectionStates.pendingHeaderSectionStateChanged(pendingChevronStateIsChanged))
  }

  fun agreedToVisitChevronStateIsChanged(agreedToVisitChevronStateIsChanged: Boolean): OverdueModel {
    return copy(overdueListSectionStates = overdueListSectionStates.agreedToVisitSectionStateChanged(agreedToVisitChevronStateIsChanged))
  }

  fun remindToCallChevronStateIsChanged(remindToCallChevronStateIsChanged: Boolean): OverdueModel {
    return copy(overdueListSectionStates = overdueListSectionStates.remindToCallSectionStateChanged(remindToCallChevronStateIsChanged))
  }

  fun removedFromOverdueChevronStateIsChanged(removedFromOverdueChevronStateIsChanged: Boolean): OverdueModel {
    return copy(overdueListSectionStates = overdueListSectionStates.removedFromOverdueSectionStateChanged(removedFromOverdueChevronStateIsChanged))
  }

  fun moreThanAYearChevronStateIsChanged(moreThanAYearChevronStateIsChanged: Boolean): OverdueModel {
    return copy(overdueListSectionStates = overdueListSectionStates.moreThanAYearSectionStateChanged(moreThanAYearChevronStateIsChanged))
  }
}
