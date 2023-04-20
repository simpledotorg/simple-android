package org.simple.clinic.monthlyreports.list

import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.widgets.UiEvent

sealed class MonthlyReportListEvent : UiEvent

data class CurrentFacilityLoaded(val facility: Facility) : MonthlyReportListEvent()

data class MonthlyReportListFetched(val responseList: List<QuestionnaireResponse>) : MonthlyReportListEvent()

object BackButtonClicked : MonthlyReportListEvent() {
  override val analyticsName = "Monthly Reports List:Back Clicked"
}

data class MonthlyReportItemClicked(
    val questionnaireType: QuestionnaireType = MonthlyScreeningReports,
    val questionnaireResponse: QuestionnaireResponse
) : MonthlyReportListEvent() {
  override val analyticsName = "$questionnaireType: Item clicked"
}
