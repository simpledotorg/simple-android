package org.simple.clinic.home.overdue

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class OverdueUpdate: Update<OverdueModel, OverdueEvent, OverdueEffect> {

  override fun update(model: OverdueModel, event: OverdueEvent): Next<OverdueModel, OverdueEffect> {
    return noChange()
  }
}
