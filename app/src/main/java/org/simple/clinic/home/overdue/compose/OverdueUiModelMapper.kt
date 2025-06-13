package org.simple.clinic.home.overdue.compose

import org.simple.clinic.R
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.AGREED_TO_VISIT
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.MORE_THAN_A_YEAR_OVERDUE
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.PENDING_TO_CALL
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.REMIND_TO_CALL
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.REMOVED_FROM_OVERDUE
import org.simple.clinic.home.overdue.OverdueAppointmentSections
import org.simple.clinic.home.overdue.OverdueListSectionStates
import org.simple.clinic.home.overdue.PendingListState.SEE_ALL
import org.simple.clinic.home.overdue.PendingListState.SEE_LESS
import org.simple.clinic.patient.Answer
import org.simple.clinic.util.UserClock
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

class OverdueUiModelMapper {

  companion object {

    fun from(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        pendingListDefaultStateSize: Int,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueInstantSearchEnabled: Boolean,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>,
        isPatientReassignmentFeatureEnabled: Boolean,
        locale: Locale,
    ): List<OverdueUiModel> {
      val searchOverduePatientsButtonListItem = searchOverduePatientItem(
          isOverdueInstantSearchEnabled,
      )

      val pendingToCallListItem = pendingToCallItem(
          overdueAppointmentSections,
          clock,
          pendingListDefaultStateSize,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
          locale,
      )

      val agreedToVisitListItem = agreedToVisitItem(
          overdueAppointmentSections,
          clock,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
          locale,
      )

      val remindToCallListItem = remindToCallItem(
          overdueAppointmentSections,
          clock,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
          locale,
      )

      val removedFromOverdueListItem = removedFromOverdueItem(
          overdueAppointmentSections,
          clock,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
          locale,
      )

      val moreThanAnOneYearOverdueListItem = moreThanAnOneYearOverdueItem(
          overdueAppointmentSections,
          clock,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
          locale,
      )

      val dividerListItem = listOf(OverdueUiModel.Divider)

      return searchOverduePatientsButtonListItem +
          pendingToCallListItem + dividerListItem +
          agreedToVisitListItem + dividerListItem +
          remindToCallListItem + dividerListItem +
          removedFromOverdueListItem + dividerListItem +
          moreThanAnOneYearOverdueListItem
    }


    private fun searchOverduePatientItem(
        isOverdueInstantSearchEnabled: Boolean,
    ): List<OverdueUiModel> =
        if (isOverdueInstantSearchEnabled) {
          listOf(OverdueUiModel.SearchButton)
        } else {
          emptyList()
        }

    private fun moreThanAnOneYearOverdueItem(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>,
        isPatientReassignmentFeatureEnabled: Boolean,
        locale: Locale,
    ): List<OverdueUiModel> {
      val moreThanAnOneYearOverdueHeader = listOf(
          OverdueUiModel.Header(
              R.string.overdue_no_visit_in_one_year_call_header,
              overdueAppointmentSections.moreThanAnYearOverdueAppointments.size,
              overdueListSectionStates.isMoreThanAnOneYearOverdueHeader,
              MORE_THAN_A_YEAR_OVERDUE,
              locale,
          ))

      val moreThanAnOneYearOverdueListItems = expandedOverdueAppointmentList(
          overdueListSectionStates.isMoreThanAnOneYearOverdueHeader,
          overdueAppointmentSections.moreThanAnYearOverdueAppointments,
          clock,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
      )

      return moreThanAnOneYearOverdueHeader + moreThanAnOneYearOverdueListItems
    }

    private fun removedFromOverdueItem(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>,
        isPatientReassignmentFeatureEnabled: Boolean,
        locale: Locale,
    ): List<OverdueUiModel> {
      val removedFromOverdueListHeader = listOf(
          OverdueUiModel.Header(
              R.string.overdue_removed_from_list_call_header,
              overdueAppointmentSections.removedFromOverdueAppointments.size,
              overdueListSectionStates.isRemovedFromOverdueListHeaderExpanded,
              REMOVED_FROM_OVERDUE,
              locale,
          ))

      val removedFromOverdueListItems = expandedOverdueAppointmentList(
          overdueListSectionStates.isRemovedFromOverdueListHeaderExpanded,
          overdueAppointmentSections.removedFromOverdueAppointments,
          clock,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
      )

      return removedFromOverdueListHeader + removedFromOverdueListItems
    }

    private fun remindToCallItem(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>,
        isPatientReassignmentFeatureEnabled: Boolean,
        locale: Locale,
    ): List<OverdueUiModel> {
      val remindToCallHeader = listOf(
          OverdueUiModel.Header(
              R.string.overdue_remind_to_call_header,
              overdueAppointmentSections.remindToCallLaterAppointments.size,
              overdueListSectionStates.isRemindToCallLaterHeaderExpanded,
              REMIND_TO_CALL,
              locale,
          ))

      val remindToCallListItems = expandedOverdueAppointmentList(
          overdueListSectionStates.isRemindToCallLaterHeaderExpanded,
          overdueAppointmentSections.remindToCallLaterAppointments,
          clock,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
      )

      return remindToCallHeader + remindToCallListItems
    }

    private fun agreedToVisitItem(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>,
        isPatientReassignmentFeatureEnabled: Boolean,
        locale: Locale,
    ): List<OverdueUiModel> {
      val agreedToVisitHeader = listOf(
          OverdueUiModel.Header(
              R.string.overdue_agreed_to_visit_call_header,
              overdueAppointmentSections.agreedToVisitAppointments.size,
              overdueListSectionStates.isAgreedToVisitHeaderExpanded,
              AGREED_TO_VISIT,
              locale,
          ))

      val agreedToVisitListItems = expandedOverdueAppointmentList(
          overdueListSectionStates.isAgreedToVisitHeaderExpanded,
          overdueAppointmentSections.agreedToVisitAppointments,
          clock,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
      )

      return agreedToVisitHeader + agreedToVisitListItems
    }

    private fun pendingToCallItem(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        pendingListDefaultStateSize: Int,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>,
        isPatientReassignmentFeatureEnabled: Boolean,
        locale: Locale,
    ): List<OverdueUiModel> {
      val pendingAppointments = overdueAppointmentSections.pendingAppointments
      val pendingToCallHeader = listOf(
          OverdueUiModel.Header(
              R.string.overdue_pending_to_call_header,
              overdueAppointmentSections.pendingAppointments.size,
              overdueListSectionStates.isPendingHeaderExpanded,
              PENDING_TO_CALL,
              locale,
          ))

      val pendingAppointmentsContent = generatePendingAppointmentsContent(
          overdueAppointmentSections,
          clock,
          pendingListDefaultStateSize,
          overdueListSectionStates,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
      )

      val showPendingListFooter = pendingAppointments.size > pendingListDefaultStateSize && overdueListSectionStates.isPendingHeaderExpanded
      val pendingListFooterItem = if (showPendingListFooter) {
        listOf(OverdueUiModel.Footer(
            pendingListState = overdueListSectionStates.pendingListState,
        ))
      } else {
        emptyList()
      }

      return pendingToCallHeader + pendingAppointmentsContent + pendingListFooterItem
    }

    private fun generatePendingAppointmentsContent(
        overdueAppointmentSections: OverdueAppointmentSections,
        clock: UserClock,
        pendingListDefaultStateSize: Int,
        overdueListSectionStates: OverdueListSectionStates,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>,
        isPatientReassignmentFeatureEnabled: Boolean,
    ): List<OverdueUiModel> {
      val pendingAppointmentsList = when (overdueListSectionStates.pendingListState) {
        SEE_LESS -> overdueAppointmentSections.pendingAppointments.take(pendingListDefaultStateSize)
        SEE_ALL -> overdueAppointmentSections.pendingAppointments
      }

      val expandedPendingAppointmentList = expandedOverdueAppointmentList(
          overdueListSectionStates.isPendingHeaderExpanded,
          pendingAppointmentsList,
          clock,
          isOverdueSelectAndDownloadEnabled,
          selectedOverdueAppointments,
          isPatientReassignmentFeatureEnabled,
      )

      return if (pendingAppointmentsList.isEmpty() && overdueListSectionStates.isPendingHeaderExpanded) {
        listOf(OverdueUiModel.NoPendingPatients)
      } else {
        expandedPendingAppointmentList
      }
    }

    private fun expandedOverdueAppointmentList(
        isListExpanded: Boolean,
        overdueAppointment: List<OverdueAppointment>,
        clock: UserClock,
        isOverdueSelectAndDownloadEnabled: Boolean,
        selectedOverdueAppointments: Set<UUID>,
        isPatientReassignmentFeatureEnabled: Boolean,
    ): List<OverdueUiModel> {
      return if (isListExpanded) {
        overdueAppointment.map {
          val isAppointmentSelected = selectedOverdueAppointments.contains(it.appointment.uuid)
          from(
              it,
              clock,
              isOverdueSelectAndDownloadEnabled,
              isAppointmentSelected,
              isPatientReassignmentFeatureEnabled,
          )
        }
      } else {
        emptyList()
      }
    }

    private fun from(
        overdueAppointment: OverdueAppointment,
        clock: UserClock,
        isOverdueSelectAndDownloadEnabled: Boolean,
        isAppointmentSelected: Boolean,
        isPatientReassignmentFeatureEnabled: Boolean,
    ): OverdueUiModel {
      return OverdueUiModel.Patient(
          appointmentUuid = overdueAppointment.appointment.uuid,
          patientUuid = overdueAppointment.appointment.patientUuid,
          name = overdueAppointment.fullName,
          gender = overdueAppointment.gender,
          age = overdueAppointment.ageDetails.estimateAge(clock),
          phoneNumber = overdueAppointment.phoneNumber?.number,
          overdueDays = daysBetweenNowAndDate(overdueAppointment.appointment.scheduledDate, clock),
          villageName = overdueAppointment.patientAddress.colonyOrVillage,
          isOverdueSelectAndDownloadEnabled = isOverdueSelectAndDownloadEnabled,
          isAppointmentSelected = isAppointmentSelected,
          isEligibleForReassignment = (overdueAppointment.eligibleForReassignment == Answer.Yes) && isPatientReassignmentFeatureEnabled,
      )
    }

    private fun daysBetweenNowAndDate(
        date: LocalDate,
        clock: UserClock
    ): Int {
      return ChronoUnit.DAYS.between(date, LocalDate.now(clock)).toInt()
    }
  }
}

