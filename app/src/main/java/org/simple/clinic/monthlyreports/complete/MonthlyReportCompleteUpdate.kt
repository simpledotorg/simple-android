package org.simple.clinic.monthlyreports.complete

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class MonthlyReportCompleteUpdate :
    Update<MonthlyReportCompleteModel, MonthlyReportCompleteEvent, MonthlyReportCompleteEffect> {
  override fun update(model: MonthlyReportCompleteModel, event: MonthlyReportCompleteEvent):
      Next<MonthlyReportCompleteModel, MonthlyReportCompleteEffect> {
    return when (event) {
      is DoneButtonClicked -> dispatch(GoToMonthlyReportsScreen)
      is QuestionnaireResponseFetched -> Next.next(model.questionnaireResponseLoaded(event.questionnaireResponse))
    }
  }
}
