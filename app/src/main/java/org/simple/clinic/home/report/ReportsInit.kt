package org.simple.clinic.home.report

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class ReportsInit : Init<ReportsModel, ReportsEffect> {
  override fun init(model: ReportsModel): First<ReportsModel, ReportsEffect> {
    return first(model, LoadReports)
  }
}
