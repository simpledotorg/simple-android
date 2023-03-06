package org.simple.clinic.monthlyscreeningreports.list

import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class MonthlyScreeningReportListEvent : UiEvent

data class CurrentFacilityLoaded(val facility: Facility) : MonthlyScreeningReportListEvent()

data class MonthlyScreeningReportListFetched(val responseList: List<QuestionnaireResponse>) : MonthlyScreeningReportListEvent()

object BackButtonClicked : MonthlyScreeningReportListEvent() {
  override val analyticsName = "Monthly Screening Reports List:Back Clicked"
}

data class MonthlyScreeningReportItemClicked(val uuid: UUID) : MonthlyScreeningReportListEvent() {
  override val analyticsName = "Monthly Screening Reports: Item clicked"
}