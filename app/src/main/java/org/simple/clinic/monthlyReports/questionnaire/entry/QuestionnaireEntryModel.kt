package org.simple.clinic.monthlyReports.questionnaire.entry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.monthlyReports.questionnaire.Questionnaire

@Parcelize
data class QuestionnaireEntryModel(
    val questionnaire: Questionnaire?
) : Parcelable {
  companion object {
    fun default() = QuestionnaireEntryModel(
        questionnaire = null,
    )
  }

  fun formLoaded(questionnaire: Questionnaire): QuestionnaireEntryModel {
    return copy(questionnaire = questionnaire)
  }
}
