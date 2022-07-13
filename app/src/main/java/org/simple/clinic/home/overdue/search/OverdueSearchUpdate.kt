package org.simple.clinic.home.overdue.search

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Empty
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.LengthTooShort
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Valid
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
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
      is OverdueAppointmentCheckBoxClicked -> overdueAppointmentCheckBoxClicked(model, event)
      is DownloadOverdueListClicked -> downloadOverdueListClicked(model, event)
    }
  }

  private fun downloadOverdueListClicked(
      model: OverdueSearchModel,
      event: DownloadOverdueListClicked
  ): Next<OverdueSearchModel, OverdueSearchEffect> {
    val appointmentIds = model.selectedOverdueAppointments.ifEmpty { event.appointmentIds }

    return if (!canGeneratePdf) dispatch(ScheduleDownload(CSV, appointmentIds)) else noChange()
  }

  private fun overdueAppointmentCheckBoxClicked(
      model: OverdueSearchModel,
      event: OverdueAppointmentCheckBoxClicked
  ): Next<OverdueSearchModel, OverdueSearchEffect> {
    val appointmentId = event.appointmentId
    val updatedSelectedAppointments = if (model.selectedOverdueAppointments.contains(appointmentId)) {
      model.selectedOverdueAppointments.filter { it != appointmentId }.toSet()
    } else {
      model.selectedOverdueAppointments + setOf(appointmentId)
    }

    return next(model.selectedOverdueAppointmentsChanged(updatedSelectedAppointments))
  }

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
