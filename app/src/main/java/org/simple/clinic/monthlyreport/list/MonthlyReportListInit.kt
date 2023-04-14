package org.simple.clinic.monthlyreport.list

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class MonthlyReportListInit : Init<MonthlyReportListModel, MonthlyReportListEffect> {
  override fun init(model: MonthlyReportListModel):
      First<MonthlyReportListModel, MonthlyReportListEffect> {
    return first(model, LoadCurrentFacility, LoadMonthlyReportListEffect)
  }
}
