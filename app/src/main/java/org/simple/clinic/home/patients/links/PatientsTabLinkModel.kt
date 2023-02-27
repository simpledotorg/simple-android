package org.simple.clinic.home.patients.links

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.questionnaire.Questionnaire
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Parcelize
data class PatientsTabLinkModel(
    val questionnaire: Questionnaire?,
    val questionnaireResponseList: List<QuestionnaireResponse>?
) : Parcelable {
  companion object {
    fun default() = PatientsTabLinkModel(
        questionnaire = null,
        questionnaireResponseList = null
    )
  }

  fun formLoaded(questionnaire: Questionnaire): PatientsTabLinkModel {
    return copy(questionnaire = questionnaire)
  }

  fun reportListLoaded(questionnaireResponseList: List<QuestionnaireResponse>): PatientsTabLinkModel {
    return copy(questionnaireResponseList = questionnaireResponseList)
  }
}

