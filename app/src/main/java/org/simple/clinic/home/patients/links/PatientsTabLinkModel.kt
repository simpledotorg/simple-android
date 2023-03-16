package org.simple.clinic.home.patients.links

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaire.Questionnaire
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Parcelize
data class PatientsTabLinkModel(
    val facility: Facility?,
    val monthlyScreeningReportsResponseList: List<QuestionnaireResponse>?,
    val monthlyScreeningReportsForm: Questionnaire?,
) : Parcelable {
  companion object {
    fun default() = PatientsTabLinkModel(
        facility = null,
        monthlyScreeningReportsResponseList = null,
        monthlyScreeningReportsForm = null
    )
  }

  private val monthlyScreeningReportsEnabled: Boolean
    get() = facility?.config?.monthlyScreeningReportsEnabled == true

  private val isMonthlyScreeningReportResponseListNotEmpty: Boolean
    get() = monthlyScreeningReportsResponseList?.isNotEmpty() == true

  private val isMonthlyScreeningReportFormPresent: Boolean
    get() = monthlyScreeningReportsForm != null

  val showMonthlyScreeningLink: Boolean
    get() = monthlyScreeningReportsEnabled &&
        isMonthlyScreeningReportResponseListNotEmpty &&
        isMonthlyScreeningReportFormPresent

  fun currentFacilityLoaded(facility: Facility): PatientsTabLinkModel {
    return copy(facility = facility)
  }

  fun monthlyScreeningReportResponseListLoaded(
      questionnaireResponseList: List<QuestionnaireResponse>
  ): PatientsTabLinkModel {
    return copy(monthlyScreeningReportsResponseList = questionnaireResponseList)
  }

  fun monthlyScreeningReportFormLoaded(
      questionnaireForm: Questionnaire
  ): PatientsTabLinkModel {
    return copy(monthlyScreeningReportsForm = questionnaireForm)
  }
}

