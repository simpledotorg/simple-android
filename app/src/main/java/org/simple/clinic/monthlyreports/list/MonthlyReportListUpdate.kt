package org.simple.clinic.monthlyreports.list

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class MonthlyReportListUpdate :
    Update<MonthlyReportListModel, MonthlyReportListEvent, MonthlyReportListEffect> {
  override fun update(model: MonthlyReportListModel, event: MonthlyReportListEvent):
      Next<MonthlyReportListModel, MonthlyReportListEffect> {
    return when (event) {
      is BackButtonClicked -> Next.dispatch(setOf(GoBack))
      is CurrentFacilityLoaded -> Next.next(model.currentFacilityLoaded(event.facility))
      is MonthlyReportListFetched -> Next.next(model.responseListLoaded(event.responseList))
      is MonthlyReportItemClicked -> dispatch(OpenMonthlyReportForm(event.questionnaireType, event.questionnaireResponse))
    }
  }
}
