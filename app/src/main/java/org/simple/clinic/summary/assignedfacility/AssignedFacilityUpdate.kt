package org.simple.clinic.summary.assignedfacility

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.util.toNullable

class AssignedFacilityUpdate : Update<AssignedFacilityModel, AssignedFacilityEvent, AssignedFacilityEffect> {

  override fun update(
      model: AssignedFacilityModel,
      event: AssignedFacilityEvent
  ): Next<AssignedFacilityModel, AssignedFacilityEffect> {
    return when (event) {
      is AssignedFacilityLoaded -> next(model.assignedFacilityUpdated(event.facility.toNullable()))
      ChangeAssignedFacilityButtonClicked -> dispatch(OpenFacilitySelection)
      is AssignedFacilitySelected -> next(
          model.assignedFacilityUpdated(event.facility),
          ChangeAssignedFacility(model.patientUuid, event.facility.uuid)
      )
    }
  }
}
