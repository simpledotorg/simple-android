package org.simple.clinic.monthlyscreeningreports.form

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaire.Questionnaire
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

@Parcelize
data class QuestionnaireEntryModel(
    val facility: Facility?,
    val questionnaire: Questionnaire?,
    val questionnaireResponse: QuestionnaireResponse?
) : Parcelable {
  companion object {
    fun default() = QuestionnaireEntryModel(
        facility = null,
        questionnaire = null,
        questionnaireResponse = null
    )
  }

  val hasFacility: Boolean
    get() = facility != null

  fun currentFacilityLoaded(facility: Facility): QuestionnaireEntryModel {
    return copy(facility = facility)
  }

  fun formLoaded(questionnaire: Questionnaire): QuestionnaireEntryModel {
    return copy(questionnaire = questionnaire)
  }

  fun responseLoaded(questionnaireResponse: QuestionnaireResponse): QuestionnaireEntryModel {
    return copy(questionnaireResponse = questionnaireResponse)
  }
}
