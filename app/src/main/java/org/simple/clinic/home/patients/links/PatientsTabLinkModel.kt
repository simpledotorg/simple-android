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
    val questionnaireResponseSections: QuestionnaireResponseSections? = null,
) : Parcelable {
  companion object {
    fun default() = PatientsTabLinkModel(
        facility = null,
        questionnaires = null,
        questionnaireResponseSections = null
    )
  }

  val showMonthlyScreeningLink: Boolean
    get() = facility?.config?.monthlyScreeningReportsEnabled == true &&
        questionnaireResponseSections?.screeningQuestionnaireResponseList?.isNotEmpty() == true &&
        questionnaires?.screeningQuestionnaire != null

  val showMonthlySuppliesLink: Boolean
    get() = facility?.config?.monthlySuppliesReportsEnabled == true &&
        questionnaireResponseSections?.suppliesQuestionnaireResponseList?.isNotEmpty() == true &&
        questionnaires?.suppliesQuestionnaire != null

  fun currentFacilityLoaded(facility: Facility): PatientsTabLinkModel {
    return copy(facility = facility)
  }

  fun questionnairesLoaded(
      questionnaires: QuestionnaireSections
  ): PatientsTabLinkModel {
    return copy(questionnaires = questionnaires)
  }

  fun questionnairesResponsesLoaded(
      questionnaireResponseSections: QuestionnaireResponseSections
  ): PatientsTabLinkModel {
    return copy(questionnaireResponseSections = questionnaireResponseSections)
  }
}

