package org.simple.clinic.monthlyreport.complete

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class MonthlyScreeningReportCompleteUpdate :
    Update<MonthlyScreeningReportCompleteModel, MonthlyScreeningReportCompleteEvent, MonthlyScreeningReportCompleteEffect> {
  override fun update(model: MonthlyScreeningReportCompleteModel, event: MonthlyScreeningReportCompleteEvent):
      Next<MonthlyScreeningReportCompleteModel, MonthlyScreeningReportCompleteEffect> {
    return when (event) {
      is DoneButtonClicked -> dispatch(GoToMonthlyScreeningReportListScreen)
      is QuestionnaireResponseFetched -> Next.next(model.questionnaireResponseLoaded(event.questionnaireResponse))
    }
  }
}
