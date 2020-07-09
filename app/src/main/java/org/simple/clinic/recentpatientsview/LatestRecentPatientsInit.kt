package org.simple.clinic.recentpatientsview

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class LatestRecentPatientsInit : Init<LatestRecentPatientsModel, LatestRecentPatientsEffect> {

  override fun init(model: LatestRecentPatientsModel): First<LatestRecentPatientsModel, LatestRecentPatientsEffect> {
    return first(model)
  }
}
