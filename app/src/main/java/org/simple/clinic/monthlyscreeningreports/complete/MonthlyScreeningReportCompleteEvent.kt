package org.simple.clinic.monthlyscreeningreports.complete

import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.widgets.UiEvent

sealed class MonthlyScreeningReportCompleteEvent : UiEvent

data class QuestionnaireResponseFetched(
    val questionnaireResponse: QuestionnaireResponse
) : MonthlyScreeningReportCompleteEvent()

object DoneButtonClicked : MonthlyScreeningReportCompleteEvent() {
  override val analyticsName = "Monthly Screening Reports Complete:Done Clicked"
}

