package org.simple.clinic.monthlyreports.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Parcelize
data class MonthlyReportListModel(
    val facility: Facility?,
    val questionnaireResponses: List<QuestionnaireResponse>?
) : Parcelable {
  companion object {
    fun default() = MonthlyReportListModel(
        facility = null,
        questionnaireResponses = null,
    )
  }

  val hasFacility: Boolean
    get() = facility != null

  fun currentFacilityLoaded(facility: Facility): MonthlyReportListModel {
    return copy(facility = facility)
  }

  fun responseListLoaded(questionnaireResponses: List<QuestionnaireResponse>): MonthlyReportListModel {
    return copy(questionnaireResponses = questionnaireResponses)
  }
}
