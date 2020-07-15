package org.simple.clinic.home.report

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class ReportsUpdate : Update<ReportsModel, ReportsEvent, ReportsEffect> {
  override fun update(model: ReportsModel, event: ReportsEvent): Next<ReportsModel, ReportsEffect> {
    return noChange()
  }
}
