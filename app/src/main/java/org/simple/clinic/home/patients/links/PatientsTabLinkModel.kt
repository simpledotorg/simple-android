package org.simple.clinic.home.patients.links

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Parcelize
data class PatientsTabLinkModel(
    val facility: Facility?,
    val questionnaireResponseList: List<QuestionnaireResponse>?
) : Parcelable {
  companion object {
    fun default() = PatientsTabLinkModel(
        facility = null,
        questionnaireResponseList = null
    )
  }

  private val monthlyScreeningReportsEnabled: Boolean
    get() = facility?.config?.monthlyScreeningReportsEnabled == true

  private val isMonthlyScreeningReportResponseListNotEmpty: Boolean
    get() = questionnaireResponseList?.isNotEmpty() == true

  val showMonthlyScreeningLink: Boolean
    get() = monthlyScreeningReportsEnabled && isMonthlyScreeningReportResponseListNotEmpty

  fun currentFacilityLoaded(facility: Facility): PatientsTabLinkModel {
    return copy(facility = facility)
  }

  fun monthlyScreeningReportResponseListLoaded(
      questionnaireResponseList: List<QuestionnaireResponse>
  ): PatientsTabLinkModel {
    return copy(questionnaireResponseList = questionnaireResponseList)
  }
}

