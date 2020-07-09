package org.simple.clinic.recentpatientsview

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class LatestRecentPatientsUpdate : Update<LatestRecentPatientsModel, LatestRecentPatientsEvent, LatestRecentPatientsEffect> {

  override fun update(
      model: LatestRecentPatientsModel,
      event: LatestRecentPatientsEvent
  ): Next<LatestRecentPatientsModel, LatestRecentPatientsEffect> {
    return noChange()
  }
}
