package org.simple.clinic.monthlyreports.list

import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.widgets.UiEvent

sealed class MonthlyReportsEvent : UiEvent

data class CurrentFacilityLoaded(val facility: Facility) : MonthlyReportsEvent()

data class MonthlyReportsFetched(val responseList: List<QuestionnaireResponse>) : MonthlyReportsEvent()

object BackButtonClicked : MonthlyReportsEvent() {
  override val analyticsName = "Monthly Reports List:Back Clicked"
}

data class MonthlyReportItemClicked(
    val questionnaireType: QuestionnaireType,
    val questionnaireResponse: QuestionnaireResponse
) : MonthlyReportsEvent() {
  override val analyticsName = "$questionnaireType: Item clicked"
}
