package org.simple.clinic.home.overdue

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.AGREED_TO_VISIT
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.MORE_THAN_A_YEAR_OVERDUE
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.PENDING_TO_CALL
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.REMIND_TO_CALL
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle.REMOVED_FROM_OVERDUE
import org.simple.clinic.home.overdue.PendingListState.SEE_ALL
import org.simple.clinic.home.overdue.PendingListState.SEE_LESS
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
import java.time.LocalDate
import java.util.UUID

class OverdueUpdate(
    private val date: LocalDate,
    private val canGeneratePdf: Boolean,
    private val isOverdueSectionsFeatureEnabled: Boolean
) : Update<OverdueModel, OverdueEvent, OverdueEffect> {

  override fun update(model: OverdueModel, event: OverdueEvent): Next<OverdueModel, OverdueEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> currentFacilityLoaded(model, event)
      is CallPatientClicked -> dispatch(OpenContactPatientScreen(event.patientUuid))
      is OverduePatientClicked -> dispatch(OpenPatientSummary(event.patientUuid))
      is OverdueAppointmentsLoaded_Old -> dispatch(ShowOverdueAppointments(event.overdueAppointmentsOld, model.isDiabetesManagementEnabled))
      is DownloadOverdueListClicked -> downloadOverdueListClicked(event, model)
      is ShareOverdueListClicked -> shareOverdueListClicked(event, model)
      is OverdueAppointmentsLoaded -> overdueAppointmentsLoaded(event, model)
      PendingListFooterClicked -> pendingListFooterClicked(model)
      is ChevronClicked -> chevronClicked(model, event.overdueAppointmentSectionTitle)
      OverdueSearchButtonClicked -> dispatch(OpenOverdueSearch)
      is OverdueAppointmentCheckBoxClicked -> dispatch(ToggleOverdueAppointmentSelection(event.appointmentId))
      ClearSelectedOverdueAppointmentsClicked -> dispatch(ClearSelectedOverdueAppointments)
      is SelectedOverdueAppointmentsLoaded -> next(model.selectedOverdueAppointmentsChanged(event.selectedAppointmentIds))
    }
  }

  private fun chevronClicked(model: OverdueModel, overdueAppointmentSectionTitle: OverdueAppointmentSectionTitle): Next<OverdueModel, OverdueEffect> {
    val updatedModel = when (overdueAppointmentSectionTitle) {
      PENDING_TO_CALL -> {
        pendingChevronStateIsChanged(model)
      }
      AGREED_TO_VISIT -> {
        agreedToVisitChevronStateIsChanged(model)
      }
      REMIND_TO_CALL -> {
        remindToCallChevronStateIsChanged(model)
      }
      REMOVED_FROM_OVERDUE -> {
        removedFromOverdueChevronStateIsChanged(model)
      }
      MORE_THAN_A_YEAR_OVERDUE -> {
        moreThanAYearChevronStateIsChanged(model)
      }
    }
    return next(updatedModel)
  }

  private fun pendingChevronStateIsChanged(model: OverdueModel): OverdueModel {
    return if (model.pendingHeaderExpanded) {
      model.pendingChevronStateIsChanged(false)
    } else {
      model.pendingChevronStateIsChanged(true)
    }
  }

  private fun agreedToVisitChevronStateIsChanged(model: OverdueModel): OverdueModel {
    return if (model.agreedToVisitHeaderExpanded) {
      model.agreedToVisitChevronStateIsChanged(false)
    } else {
      model.agreedToVisitChevronStateIsChanged(true)
    }
  }

  private fun remindToCallChevronStateIsChanged(model: OverdueModel): OverdueModel {
    return if (model.remindToCallLaterHeaderExpanded) {
      model.remindToCallChevronStateIsChanged(false)
    } else {
      model.remindToCallChevronStateIsChanged(true)
    }
  }

  private fun removedFromOverdueChevronStateIsChanged(model: OverdueModel): OverdueModel {
    return if (model.removedFromOverdueListHeaderExpanded) {
      model.removedFromOverdueChevronStateIsChanged(false)
    } else {
      model.removedFromOverdueChevronStateIsChanged(true)
    }
  }

  private fun moreThanAYearChevronStateIsChanged(model: OverdueModel): OverdueModel {
    return if (model.moreThanAnOneYearOverdueHeader) {
      model.moreThanAYearChevronStateIsChanged(false)
    } else {
      model.moreThanAYearChevronStateIsChanged(true)
    }
  }

  private fun pendingListFooterClicked(model: OverdueModel): Next<OverdueModel, OverdueEffect> {
    val changedState = when (model.overdueListSectionStates.pendingListState) {
      SEE_ALL -> SEE_LESS
      SEE_LESS -> SEE_ALL
    }

    return next(model.pendingListStateChanged(changedState))
  }

  private fun currentFacilityLoaded(model: OverdueModel, event: CurrentFacilityLoaded): Next<OverdueModel, OverdueEffect> {
    val facilityLoadedModel = model.currentFacilityLoaded(event.facility)
    return if (isOverdueSectionsFeatureEnabled) {
      next(facilityLoadedModel, LoadOverdueAppointments(date, event.facility))
    } else {
      next(facilityLoadedModel, LoadOverdueAppointments_old(date, event.facility))
    }
  }

  private fun overdueAppointmentsLoaded(
      event: OverdueAppointmentsLoaded,
      model: OverdueModel
  ): Next<OverdueModel, OverdueEffect> {
    return next(model.overdueAppointmentsLoaded(
        overdueAppointmentSections = event.overdueAppointmentSections
    ))
  }

  private fun shareOverdueListClicked(event: ShareOverdueListClicked, model: OverdueModel): Next<OverdueModel, OverdueEffect> {
    val effect = if (event.hasNetworkConnection) {
      openDialogForShareEffect(model.selectedOverdueAppointments)
    } else {
      ShowNoActiveNetworkConnectionDialog
    }

    return dispatch(effect)
  }

  private fun downloadOverdueListEffect(selectedAppointmentIds: Set<UUID>): OverdueEffect {
    return if (canGeneratePdf)
      OpenSelectDownloadFormatDialog(selectedAppointmentIds)
    else
      ScheduleDownload(fileFormat = CSV)
  }

  private fun openDialogForShareEffect(selectedAppointmentIds: Set<UUID>): OverdueEffect {
    return if (canGeneratePdf)
      OpenSelectShareFormatDialog(selectedAppointmentIds)
    else
      OpenSharingInProgressDialog(selectedAppointmentIds)
  }

  private fun downloadOverdueListClicked(
      event: DownloadOverdueListClicked,
      model: OverdueModel
  ): Next<OverdueModel, OverdueEffect> {
    val effect = if (event.hasNetworkConnection) {
      downloadOverdueListEffect(selectedAppointmentIds = model.selectedOverdueAppointments)
    } else {
      ShowNoActiveNetworkConnectionDialog
    }

    return dispatch(effect)
  }
}
