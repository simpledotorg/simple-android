package org.simple.clinic.home.patients.links

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaire.QuestionnaireResponseSections
import org.simple.clinic.questionnaire.QuestionnaireSections

@Parcelize
data class PatientsTabLinkModel(
    val facility: Facility?,
    @IgnoredOnParcel
    val questionnaires: QuestionnaireSections? = null,
    @IgnoredOnParcel
    val questionnaireResponses: QuestionnaireResponseSections? = null,
) : Parcelable {
  companion object {
    fun default() = PatientsTabLinkModel(
        facility = null,
        questionnaires = null,
        questionnaireResponses = null
    )
  }

  val showMonthlyScreeningLink: Boolean
    get() = facility?.config?.monthlyScreeningReportsEnabled == true &&
        questionnaireResponses?.screeningQuestionnaireResponseList?.isNotEmpty() == true &&
        questionnaires?.screeningQuestionnaire != null

  val showMonthlySuppliesLink: Boolean
    get() = facility?.config?.monthlySuppliesReportsEnabled == true &&
        questionnaireResponses?.suppliesQuestionnaireResponseList?.isNotEmpty() == true &&
        questionnaires?.suppliesQuestionnaire != null

  val hasDrugStockReportContent: Boolean
    get() = !questionnaireResponses?.drugStockReportsResponseList.isNullOrEmpty() &&
        questionnaires?.drugStockReportsQuestionnaire != null

  fun currentFacilityLoaded(facility: Facility): PatientsTabLinkModel {
    return copy(facility = facility)
  }

  fun questionnairesLoaded(
      questionnaires: QuestionnaireSections
  ): PatientsTabLinkModel {
    return copy(questionnaires = questionnaires)
  }

  fun questionnaireResponsesLoaded(
      questionnaireResponseSections: QuestionnaireResponseSections
  ): PatientsTabLinkModel {
    return copy(questionnaireResponses = questionnaireResponseSections)
  }
}

