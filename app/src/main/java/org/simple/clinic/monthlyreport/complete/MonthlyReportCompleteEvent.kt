package org.simple.clinic.monthlyreport.complete

import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.widgets.UiEvent

sealed class MonthlyReportCompleteEvent : UiEvent

data class QuestionnaireResponseFetched(
    val questionnaireResponse: QuestionnaireResponse
) : MonthlyReportCompleteEvent()

object DoneButtonClicked : MonthlyReportCompleteEvent() {
  override val analyticsName = "Monthly Screening Reports Complete:Done Clicked"
}

