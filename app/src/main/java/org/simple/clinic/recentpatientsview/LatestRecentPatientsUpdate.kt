package org.simple.clinic.recentpatientsview

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class LatestRecentPatientsUpdate : Update<LatestRecentPatientsModel, LatestRecentPatientsEvent, LatestRecentPatientsEffect> {

  override fun update(
      model: LatestRecentPatientsModel,
      event: LatestRecentPatientsEvent
  ): Next<LatestRecentPatientsModel, LatestRecentPatientsEffect> {
    return when(event) {
      is RecentPatientsLoaded -> next(model.recentPatientsLoaded(event.recentPatients))
    }
  }
}
