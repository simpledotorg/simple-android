package org.simple.clinic.summary.assignedfacility

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class AssignedFacilityInit : Init<AssignedFacilityModel, AssignedFacilityEffect> {
  override fun init(model: AssignedFacilityModel): First<AssignedFacilityModel, AssignedFacilityEffect> {
    return if (model.hasAssignedFacility) {
      first(model)
    } else {
      first(model, LoadAssignedFacility(model.patientUuid))
    }
  }
}
