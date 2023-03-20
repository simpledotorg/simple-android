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
    val questionnaireResponse: QuestionnaireResponse
) : Parcelable {
  companion object {
    fun from(questionnaireResponse: QuestionnaireResponse) = QuestionnaireEntryModel(
        facility = null,
        questionnaire = null,
        questionnaireResponse = questionnaireResponse
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

  fun updateContent(content: MutableMap<String, Any?>): QuestionnaireEntryModel {
    setSubmitStatusToTrue(content)
    return copy(
        questionnaireResponse = questionnaireResponse.copy(
            content = content,
            questionnaireId = requireNotNull(questionnaire).uuid
        )
    )
  }

  private fun setSubmitStatusToTrue(content: MutableMap<String, Any?>) {
    content["submitted"] = true
  }
}
