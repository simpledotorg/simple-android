package org.simple.clinic.home.overdue

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.analytics.NetworkConnectivityStatus
import org.simple.clinic.analytics.NetworkConnectivityStatus.ACTIVE
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import java.time.LocalDate

class OverdueUpdate(
    val date: LocalDate
) : Update<OverdueModel, OverdueEvent, OverdueEffect> {

  override fun update(model: OverdueModel, event: OverdueEvent): Next<OverdueModel, OverdueEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> loadOverduePatients(model, event)
      is CallPatientClicked -> dispatch(OpenContactPatientScreen(event.patientUuid))
      is OverduePatientClicked -> dispatch(OpenPatientSummary(event.patientUuid))
      is OverdueAppointmentsLoaded -> dispatch(ShowOverdueAppointments(event.overdueAppointments, model.isDiabetesManagementEnabled))
      DownloadOverdueListClicked -> downloadOverdueListClicked()
      ShareOverdueListClicked -> shareOverdueListClicked()
      is NetworkConnectivityStatusLoaded -> networkConnectivityStatusLoaded(event.status)
    }
  }

  private fun networkConnectivityStatusLoaded(status: NetworkConnectivityStatus): Next<OverdueModel, OverdueEffect> {
    return if (status == ACTIVE) {
      noChange()
    } else {
      dispatch(ShowNoActiveNetworkConnectionDialog)
    }
  }

  private fun shareOverdueListClicked(): Next<OverdueModel, OverdueEffect> {
    return dispatch(LoadNetworkConnectivityStatus)
  }

  private fun downloadOverdueListClicked(): Next<OverdueModel, OverdueEffect> {
    return dispatch(LoadNetworkConnectivityStatus)
  }

  private fun loadOverduePatients(
      model: OverdueModel,
      event: CurrentFacilityLoaded
  ): Next<OverdueModel, OverdueEffect> =
      next(model.currentFacilityLoaded(event.facility), LoadOverdueAppointments(date, event.facility))
}
