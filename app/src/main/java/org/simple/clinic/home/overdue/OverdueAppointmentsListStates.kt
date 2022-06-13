package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OverdueListSectionStates(
    val pendingListState: PendingListState,
    val isPendingHeaderExpanded: Boolean,
    val isAgreedToVisitHeaderExpanded: Boolean,
    val isRemindToCallLaterHeaderExpanded: Boolean,
    val isRemovedFromOverdueListHeaderExpanded: Boolean,
    val isMoreThanAnOneYearOverdueHeader: Boolean
) : Parcelable {

  fun pendingListStateChanged(pendingListState: PendingListState): OverdueListSectionStates =
      copy(pendingListState = pendingListState)

  fun pendingHeaderSectionStateChanged(isPendingHeaderExpanded: Boolean): OverdueListSectionStates =
      copy(isPendingHeaderExpanded = isPendingHeaderExpanded)

  fun agreedToVisitSectionStateChanged(isAgreedToVisitHeaderExpanded: Boolean): OverdueListSectionStates =
      copy(isAgreedToVisitHeaderExpanded = isAgreedToVisitHeaderExpanded)

  fun remindToCallSectionStateChanged(isRemindToCallLaterHeaderExpanded: Boolean): OverdueListSectionStates =
      copy(isRemindToCallLaterHeaderExpanded = isRemindToCallLaterHeaderExpanded)

  fun removedFromOverdueSectionStateChanged(isRemovedFromOverdueListHeaderExpanded: Boolean): OverdueListSectionStates =
      copy(isRemovedFromOverdueListHeaderExpanded = isRemovedFromOverdueListHeaderExpanded)

  fun moreThanAYearSectionStateChanged(isMoreThanAnOneYearOverdueHeader: Boolean): OverdueListSectionStates =
      copy(isMoreThanAnOneYearOverdueHeader = isMoreThanAnOneYearOverdueHeader)
}
