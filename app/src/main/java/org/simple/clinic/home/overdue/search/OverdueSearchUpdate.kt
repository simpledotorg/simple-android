package org.simple.clinic.home.overdue.search

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.home.overdue.search.OverdueButtonType.DOWNLOAD
import org.simple.clinic.home.overdue.search.OverdueButtonType.SELECT_ALL
import org.simple.clinic.home.overdue.search.OverdueButtonType.SHARE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import java.time.LocalDate

class OverdueSearchUpdate(
    private val date: LocalDate,
    private val canGeneratePdf: Boolean
) : Update<OverdueSearchModel, OverdueSearchEvent, OverdueSearchEffect> {

  override fun update(model: OverdueSearchModel, event: OverdueSearchEvent): Next<OverdueSearchModel, OverdueSearchEffect> {
    return when (event) {
      is OverdueSearchResultsLoaded -> next(model.overdueSearchResultsLoaded(event.overdueAppointments))
      is CallPatientClicked -> dispatch(OpenContactPatientSheet(event.patientUuid))
      is OverduePatientClicked -> dispatch(OpenPatientSummary(event.patientUuid))
      is OverdueSearchLoadStateChanged -> next(model.loadStateChanged(event.overdueSearchProgressState))
      is OverdueAppointmentCheckBoxClicked -> dispatch(ToggleOverdueAppointmentSelection(event.appointmentId))
      ClearSelectedOverdueAppointmentsClicked -> dispatch(ClearSelectedOverdueAppointments)
      is SelectedOverdueAppointmentsLoaded -> next(model.selectedOverdueAppointmentsChanged(event.selectedAppointmentIds))
      is SelectedAppointmentIdsReplaced -> selectedAppointmentIdsReplaced(event)
      is DownloadButtonClicked -> downloadButtonClicked(model, event)
      is ShareButtonClicked -> shareButtonClicked(model, event)
      is SelectAllButtonClicked -> selectAllButtonClicked(model)
      is SearchResultsAppointmentIdsLoaded -> searchResultsAppointmentIdsLoaded(model, event)
      is VillagesAndPatientNamesLoaded -> next(model.villagesAndPatientNamesLoaded(event.villagesAndPatientNames))
      is OverdueSearchInputsChanged -> searchInputsChanged(model, event)
    }
  }

  private fun searchInputsChanged(model: OverdueSearchModel, event: OverdueSearchInputsChanged): Next<OverdueSearchModel, OverdueSearchEffect> {
    return next(
        model.overdueSearchInputsChanged(event.searchInputs),
        SearchOverduePatients(
            searchInputs = event.searchInputs,
            since = date
        )
    )
  }

  private fun selectAllButtonClicked(model: OverdueSearchModel): Next<OverdueSearchModel, OverdueSearchEffect> {
    return next(
        model.loadStateChanged(IN_PROGRESS),
        LoadSearchResultsAppointmentIds(
            buttonType = SELECT_ALL,
            searchInputs = model.searchInputs,
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
              searchInputs = model.searchInputs,
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
              searchInputs = model.searchInputs,
              since = date
          )
      )
    }
  }

  private fun downloadOverdueAppointmentsEffect() = if (canGeneratePdf) OpenSelectDownloadFormatDialog else ScheduleDownload

  private fun shareOverdueAppointmentsEffect() = if (canGeneratePdf) OpenSelectShareFormatDialog else OpenShareInProgressDialog
}
