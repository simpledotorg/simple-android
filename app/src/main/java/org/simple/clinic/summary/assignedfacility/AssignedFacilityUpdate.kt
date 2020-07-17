package org.simple.clinic.summary.assignedfacility

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class AssignedFacilityUpdate : Update<AssignedFacilityModel, AssignedFacilityEvent,
    AssignedFacilityEffect> {

  override fun update(model: AssignedFacilityModel, event: AssignedFacilityEvent):
      Next<AssignedFacilityModel, AssignedFacilityEffect> = noChange()
}
