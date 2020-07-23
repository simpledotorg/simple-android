package org.simple.clinic.recentpatient

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class AllRecentPatientsInit : Init<AllRecentPatientsModel, AllRecentPatientsEffect> {

  override fun init(model: AllRecentPatientsModel): First<AllRecentPatientsModel, AllRecentPatientsEffect> {
    return first(model, LoadAllRecentPatients)
  }
}
