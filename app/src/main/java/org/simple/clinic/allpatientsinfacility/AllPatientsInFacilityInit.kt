package org.simple.clinic.allpatientsinfacility

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityModel.Companion.FETCHING_PATIENTS

class AllPatientsInFacilityInit : Init<AllPatientsInFacilityModel, AllPatientsInFacilityEffect> {
  override fun init(model: AllPatientsInFacilityModel): First<AllPatientsInFacilityModel, AllPatientsInFacilityEffect> =
      first(FETCHING_PATIENTS, setOf(FetchFacilityEffect))
}
