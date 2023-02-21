package org.simple.clinic.monthlyscreeningreports.list

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class MonthlyScreeningReportListInit(
) : Init<MonthlyScreeningReportListModel, MonthlyScreeningReportListEffect> {
  override fun init(model: MonthlyScreeningReportListModel):
      First<MonthlyScreeningReportListModel, MonthlyScreeningReportListEffect> {
    return first(model, LoadCurrentFacility, LoadMonthlyReportListEffect)
  }
}
