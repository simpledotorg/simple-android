package org.simple.clinic.monthlyscreeningreports.list

import com.spotify.mobius.Next
import com.spotify.mobius.Update

class MonthlyScreeningReportListUpdate :
    Update<MonthlyScreeningReportListModel, MonthlyScreeningReportListEvent, MonthlyScreeningReportListEffect> {
  override fun update(model: MonthlyScreeningReportListModel, event: MonthlyScreeningReportListEvent):
      Next<MonthlyScreeningReportListModel, MonthlyScreeningReportListEffect> {
    return when (event) {
      is BackButtonClicked -> Next.dispatch(setOf(GoBack))
      is CurrentFacilityLoaded -> Next.next(model.currentFacilityLoaded(event.facility))
      is MonthlyScreeningReportListFetched -> Next.next(model.responseListLoaded(event.responseList))
    }
  }
}
