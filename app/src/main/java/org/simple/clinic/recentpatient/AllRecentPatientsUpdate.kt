package org.simple.clinic.recentpatient

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class AllRecentPatientsUpdate : Update<AllRecentPatientsModel, AllRecentPatientsEvent, AllRecentPatientsEffect> {

  override fun update(model: AllRecentPatientsModel, event: AllRecentPatientsEvent): Next<AllRecentPatientsModel, AllRecentPatientsEffect> {
    return noChange()
  }
}
