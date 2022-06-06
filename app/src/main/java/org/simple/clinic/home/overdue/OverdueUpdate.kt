package org.simple.clinic.home.overdue

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV
import java.time.LocalDate

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
      is DownloadOverdueListClicked -> downloadOverdueListClicked(event)
      is ShareOverdueListClicked -> shareOverdueListClicked(event)
      is OverdueAppointmentsLoaded -> overdueAppointmentsLoaded(event, model)
    }
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
}
