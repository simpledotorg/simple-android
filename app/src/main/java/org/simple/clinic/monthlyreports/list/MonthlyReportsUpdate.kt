package org.simple.clinic.monthlyreports.list

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class MonthlyReportsUpdate :
    Update<MonthlyReportsModel, MonthlyReportsEvent, MonthlyReportsEffect> {
  override fun update(model: MonthlyReportsModel, event: MonthlyReportsEvent):
      Next<MonthlyReportsModel, MonthlyReportsEffect> {
    return when (event) {
      is BackButtonClicked -> Next.dispatch(setOf(GoBack))
      is CurrentFacilityLoaded -> Next.next(model.currentFacilityLoaded(event.facility))
      is MonthlyReportsFetched -> Next.next(model.monthlyReportsLoaded(event.responseList))
      is MonthlyReportItemClicked -> dispatch(OpenMonthlyReportForm(event.questionnaireType, event.questionnaireResponse))
    }
  }
}
