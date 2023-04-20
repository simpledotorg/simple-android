package org.simple.clinic.monthlyreports.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Parcelize
data class MonthlyReportsModel(
    val facility: Facility?,
    val questionnaireResponses: List<QuestionnaireResponse>?
) : Parcelable {
  companion object {
    fun default() = MonthlyReportsModel(
        facility = null,
        questionnaireResponses = null,
    )
  }

  val hasFacility: Boolean
    get() = facility != null

  fun currentFacilityLoaded(facility: Facility): MonthlyReportsModel {
    return copy(facility = facility)
  }

  fun monthlyReportsLoaded(questionnaireResponses: List<QuestionnaireResponse>): MonthlyReportsModel {
    return copy(questionnaireResponses = questionnaireResponses)
  }
}
