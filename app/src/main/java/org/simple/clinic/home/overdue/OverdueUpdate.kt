package org.simple.clinic.home.overdue

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
import java.time.LocalDate

class OverdueUpdate(
    val date: LocalDate,
    val canGeneratePdf: Boolean
) : Update<OverdueModel, OverdueEvent, OverdueEffect> {

  override fun update(model: OverdueModel, event: OverdueEvent): Next<OverdueModel, OverdueEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> loadOverduePatients(model, event)
      is CallPatientClicked -> dispatch(OpenContactPatientScreen(event.patientUuid))
      is OverduePatientClicked -> dispatch(OpenPatientSummary(event.patientUuid))
      is OverdueAppointmentsLoaded -> dispatch(ShowOverdueAppointments(event.overdueAppointments, model.isDiabetesManagementEnabled))
      is DownloadOverdueListClicked -> downloadOverdueListClicked(event)
      is ShareOverdueListClicked -> shareOverdueListClicked(event)
    }
  }

  private fun shareOverdueListClicked(event: ShareOverdueListClicked): Next<OverdueModel, OverdueEffect> {
    val effect = if (event.hasNetworkConnection) {
      openDialogForShareEffect()
    } else {
      ShowNoActiveNetworkConnectionDialog
    }

    return dispatch(effect)
  }

  private fun downloadOverdueListEffect(): OverdueEffect {
    return if (canGeneratePdf) OpenSelectDownloadFormatDialog else ScheduleDownload(CSV)
  }

  private fun openDialogForShareEffect(): OverdueEffect {
    return if (canGeneratePdf) OpenSelectShareFormatDialog else OpenSharingInProgressDialog
  }

  private fun downloadOverdueListClicked(event: DownloadOverdueListClicked): Next<OverdueModel, OverdueEffect> {
    val effect = if (event.hasNetworkConnection) {
      downloadOverdueListEffect()
    } else {
      ShowNoActiveNetworkConnectionDialog
    }

    return dispatch(effect)
  }

  private fun loadOverduePatients(
      model: OverdueModel,
      event: CurrentFacilityLoaded
  ): Next<OverdueModel, OverdueEffect> =
      next(model.currentFacilityLoaded(event.facility), LoadOverdueAppointments(date, event.facility))
}
