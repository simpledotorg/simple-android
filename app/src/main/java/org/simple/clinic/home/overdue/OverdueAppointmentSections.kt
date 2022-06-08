package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OverdueAppointmentSections(
    val pendingAppointments: List<OverdueAppointment>,
    val agreedToVisitAppointments: List<OverdueAppointment>,
    val remindToCallLaterAppointments: List<OverdueAppointment>,
    val removedFromOverdueAppointments: List<OverdueAppointment>,
    val moreThanAnYearOverdueAppointments: List<OverdueAppointment>,
    val isPendingHeaderExpanded: Boolean = true,
    val isAgreedToVisitHeaderExpanded: Boolean = false,
    val isRemindToCallLaterHeaderExpanded: Boolean = false,
    val isRemovedFromOverdueListHeaderExpanded: Boolean = false,
    val isMoreThanAnOneYearOverdueHeader: Boolean = false,
) : Parcelable {

  val overdueCount: Int
    get() = pendingAppointments.size + agreedToVisitAppointments.size + remindToCallLaterAppointments.size +
        removedFromOverdueAppointments.size + moreThanAnYearOverdueAppointments.size
}
