package org.simple.clinic.home.report

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class ReportsUpdate : Update<ReportsModel, ReportsEvent, ReportsEffect> {
  override fun update(model: ReportsModel, event: ReportsEvent): Next<ReportsModel, ReportsEffect> {
    return when (event) {
      is ReportsLoaded -> {
        next(model.reportsContentLoaded(event.reportsContent))
      }
      WebBackClicked -> dispatch(LoadReports)
    }
  }
}
