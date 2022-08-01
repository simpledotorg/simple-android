package org.simple.clinic.home.overdue.search

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.home.overdue.search.OverdueButtonType.DOWNLOAD
import org.simple.clinic.home.overdue.search.OverdueButtonType.SELECT_ALL
import org.simple.clinic.home.overdue.search.OverdueButtonType.SHARE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
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
      is OverdueSearchResultsLoaded -> dispatch(SetOverdueSearchPagingData(
          overdueSearchResults = event.overdueAppointments,
          selectedOverdueAppointments = model.selectedOverdueAppointments,
          searchQuery = model.searchQuery.orEmpty()
      ))
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
      is SelectAllButtonClicked -> selectAllButtonClicked(model)
      is SearchResultsAppointmentIdsLoaded -> searchResultsAppointmentIdsLoaded(model, event)
      is VillagesAndPatientNamesLoaded -> next(model.villagesAndPatientNamesLoaded(event.villagesAndPatientNames))
    }
  }

  private fun selectAllButtonClicked(model: OverdueSearchModel): Next<OverdueSearchModel, OverdueSearchEffect> {
    return next(
        model.loadStateChanged(IN_PROGRESS),
        LoadSearchResultsAppointmentIds(
            buttonType = SELECT_ALL,
            searchQuery = model.searchQuery.orEmpty(),
            since = date
        )
    )
  }

  private fun searchResultsAppointmentIdsLoaded(
      model: OverdueSearchModel,
      event: SearchResultsAppointmentIdsLoaded
  ): Next<OverdueSearchModel, OverdueSearchEffect> {
    val effect = when (event.buttonType) {
      DOWNLOAD, SHARE -> ReplaceSelectedAppointmentIds(event.searchResultsAppointmentIds, event.buttonType)
      SELECT_ALL -> SelectAllAppointmentIds(event.searchResultsAppointmentIds)
    }

    return next(model.loadStateChanged(DONE), effect)
  }

  private fun shareButtonClicked(model: OverdueSearchModel, event: ShareButtonClicked): Next<OverdueSearchModel, OverdueSearchEffect> {
    return when {
      !event.hasNetworkConnection -> dispatch(ShowNoInternetConnectionDialog)
      model.selectedOverdueAppointments.isNotEmpty() -> dispatch(shareOverdueAppointmentsEffect())
      else -> next(
          model.loadStateChanged(IN_PROGRESS),
          LoadSearchResultsAppointmentIds(
              buttonType = SHARE,
              searchQuery = model.searchQuery.orEmpty(),
              since = date
          )
      )
    }
  }

  private fun selectedAppointmentIdsReplaced(event: SelectedAppointmentIdsReplaced): Next<OverdueSearchModel, OverdueSearchEffect> {
    val effect = when (event.type) {
      DOWNLOAD -> downloadOverdueAppointmentsEffect()
      SHARE -> shareOverdueAppointmentsEffect()
      SELECT_ALL -> throw IllegalArgumentException("${event.type} cannot replace selected ids")
    }

    return dispatch(effect)
  }

  private fun downloadButtonClicked(model: OverdueSearchModel, event: DownloadButtonClicked): Next<OverdueSearchModel, OverdueSearchEffect> {
    return when {
      !event.hasNetworkConnection -> dispatch(ShowNoInternetConnectionDialog)
      model.selectedOverdueAppointments.isNotEmpty() -> dispatch(downloadOverdueAppointmentsEffect())
      else -> next(
          model.loadStateChanged(IN_PROGRESS),
          LoadSearchResultsAppointmentIds(
              buttonType = DOWNLOAD,
              searchQuery = model.searchQuery.orEmpty(),
              since = date
          )
      )
    }
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
