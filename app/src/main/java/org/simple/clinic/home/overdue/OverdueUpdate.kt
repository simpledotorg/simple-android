package org.simple.clinic.home.overdue

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import java.time.LocalDate

class OverdueUpdate(
    val date: LocalDate,
    val isOverdueListChangesEnabled: Boolean
) : Update<OverdueModel, OverdueEvent, OverdueEffect> {

  override fun update(model: OverdueModel, event: OverdueEvent): Next<OverdueModel, OverdueEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> loadOverduePatients(model, event)
      is CallPatientClicked -> dispatch(OpenContactPatientScreen(event.patientUuid))
      is PatientNameClicked -> dispatch(OpenPatientSummary(event.patientUuid))
      is OverdueAppointmentsLoaded -> dispatch(ShowOverdueAppointments(event.overdueAppointments, model.isDiabetesManagementEnabled))
    }
  }

  private fun loadOverduePatients(
      model: OverdueModel,
      event: CurrentFacilityLoaded
  ): Next<OverdueModel, OverdueEffect> =
      if (isOverdueListChangesEnabled) {
        next(model.currentFacilityLoaded(event.facility), LoadOverdueAppointments(date, event.facility))
      } else {
        next(model.currentFacilityLoaded(event.facility), LoadOverdueAppointments_old(date, event.facility))
      }
}
