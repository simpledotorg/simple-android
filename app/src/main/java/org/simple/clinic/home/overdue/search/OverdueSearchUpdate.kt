package org.simple.clinic.home.overdue.search

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.home.overdue.search.OverdueButtonType.DOWNLOAD
import org.simple.clinic.home.overdue.search.OverdueButtonType.SHARE
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Empty
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.LengthTooShort
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Valid
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import java.time.LocalDate

class OverdueSearchUpdate(
    private val date: LocalDate,
    private val canGeneratePdf: Boolean
) : Update<OverdueSearchModel, OverdueSearchEvent, OverdueSearchEffect> {

  override fun update(model: OverdueSearchModel, event: OverdueSearchEvent): Next<OverdueSearchModel, OverdueSearchEffect> {
    return when (event) {
      is OverdueSearchHistoryLoaded -> next(model.overdueSearchHistoryLoaded(event.searchHistory))
      is OverdueSearchQueryChanged -> next(
          model.overdueSearchQueryChanged(event.searchQuery),
          ValidateOverdueSearchQuery(event.searchQuery)
      )
      is OverdueSearchQueryValidated -> searchQueryValidated(event)
      is OverdueSearchResultsLoaded -> next(model.overdueSearchResultsLoaded(event.overdueAppointments))
      is CallPatientClicked -> dispatch(OpenContactPatientSheet(event.patientUuid))
      is OverduePatientClicked -> dispatch(OpenPatientSummary(event.patientUuid))
      is OverdueSearchHistoryClicked -> dispatch(SetOverdueSearchQuery(event.searchQuery))
      is OverdueSearchLoadStateChanged -> next(model.loadStateChanged(event.overdueSearchProgressState))
      OverdueSearchScreenShown -> overdueScreenShown(model)
      is OverdueAppointmentCheckBoxClicked -> dispatch(ToggleOverdueAppointmentSelection(event.appointmentId))
      ClearSelectedOverdueAppointmentsClicked -> dispatch(ClearSelectedOverdueAppointments)
      is SelectedOverdueAppointmentsLoaded -> next(model.selectedOverdueAppointmentsChanged(event.selectedAppointmentIds))
      is SelectedAppointmentIdsReplaced -> selectedAppointmentIdsReplaced(event)
      is DownloadButtonClicked -> downloadButtonClicked(model, event)
      is ShareButtonClicked -> shareButtonClicked(model, event)
    }
  }

  private fun shareButtonClicked(model: OverdueSearchModel, event: ShareButtonClicked): Next<OverdueSearchModel, OverdueSearchEffect> {
    val effect = if (model.selectedOverdueAppointments.isNotEmpty()) {
      shareOverdueAppointmentsEffect()
    } else {
      ReplaceSelectedAppointmentIds(event.searchResultsAppointmentIds, SHARE)
    }

    return dispatch(effect)
  }

  private fun selectedAppointmentIdsReplaced(event: SelectedAppointmentIdsReplaced): Next<OverdueSearchModel, OverdueSearchEffect> {
    val effect = when (event.type) {
      DOWNLOAD -> downloadOverdueAppointmentsEffect()
      SHARE -> shareOverdueAppointmentsEffect()
    }

    return dispatch(effect)
  }

  private fun downloadButtonClicked(model: OverdueSearchModel, event: DownloadButtonClicked): Next<OverdueSearchModel, OverdueSearchEffect> {
    val effect = if (model.selectedOverdueAppointments.isNotEmpty()) {
      downloadOverdueAppointmentsEffect()
    } else {
      ReplaceSelectedAppointmentIds(event.searchResultsAppointmentIds, DOWNLOAD)
    }

    return dispatch(effect)
  }

  private fun downloadOverdueAppointmentsEffect() = if (canGeneratePdf) OpenSelectDownloadFormatDialog else ScheduleDownload

  private fun shareOverdueAppointmentsEffect() = if (canGeneratePdf) OpenSelectShareFormatDialog else OpenShareInProgressDialog

  private fun overdueScreenShown(model: OverdueSearchModel): Next<OverdueSearchModel, OverdueSearchEffect> {
    return if (model.hasSearchQuery) {
      dispatch(SetOverdueSearchQuery(model.searchQuery.orEmpty()))
    } else {
      noChange()
    }
  }

  private fun searchQueryValidated(event: OverdueSearchQueryValidated): Next<OverdueSearchModel, OverdueSearchEffect> {
    return when (val result = event.result) {
      is Valid -> dispatch(AddQueryToOverdueSearchHistory(result.searchQuery), SearchOverduePatients(result.searchQuery, date))
      Empty,
      LengthTooShort -> noChange()
    }
  }
}
