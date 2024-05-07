package org.simple.clinic.reassignPatient

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.util.toNullable

class ReassignPatientUpdate : Update<ReassignPatientModel, ReassignPatientEvent, ReassignPatientEffect> {

  override fun update(
      model: ReassignPatientModel,
      event: ReassignPatientEvent
  ): Next<ReassignPatientModel, ReassignPatientEffect> {
    return when (event) {
      is AssignedFacilityLoaded -> next(model.assignedFacilityUpdated(event.facility.toNullable()))
      is NotNowClicked -> dispatch(CloseSheet)
      is ChangeClicked -> dispatch(OpenSelectFacilitySheet)
      is NewAssignedFacilitySelected -> dispatch(ChangeAssignedFacility(model.patientUuid, event.facility.uuid))
      is AssignedFacilityChanged -> Next.noChange()
    }
  }
}
