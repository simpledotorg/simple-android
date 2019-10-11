package org.simple.clinic.allpatientsinfacility

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class AllPatientsInFacilityInit : Init<AllPatientsInFacilityModel, AllPatientsInFacilityEffect> {
  override fun init(model: AllPatientsInFacilityModel): First<AllPatientsInFacilityModel, AllPatientsInFacilityEffect> {
    return first(model, setOf(FetchFacilityEffect))
  }
}