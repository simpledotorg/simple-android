package org.simple.clinic.monthlyscreeningreports.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Parcelize
data class MonthlyScreeningReportListModel(
    val facility: Facility?,
    val questionnaireResponses: List<QuestionnaireResponse>?
) : Parcelable {
  companion object {
    fun default() = MonthlyScreeningReportListModel(
        facility = null,
        questionnaireResponses = null,
    )
  }

  val hasFacility: Boolean
    get() = facility != null

  fun currentFacilityLoaded(facility: Facility): MonthlyScreeningReportListModel {
    return copy(facility = facility)
  }

  fun responseListLoaded(questionnaireResponses: List<QuestionnaireResponse>): MonthlyScreeningReportListModel {
    return copy(questionnaireResponses = questionnaireResponses)
  }
}
