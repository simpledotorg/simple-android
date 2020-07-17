package org.simple.clinic.summary.assignedfacility

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class AssignedFacilityInit : Init<AssignedFacilityModel, AssignedFacilityEffect> {
  override fun init(model: AssignedFacilityModel): First<AssignedFacilityModel,
      AssignedFacilityEffect> = first(model)
}
