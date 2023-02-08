package org.simple.clinic.monthlyReports.questionnaire.entry

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireType

class QuestionnaireEntryInit(
    private val questionnaireType: QuestionnaireType
) : Init<QuestionnaireEntryModel, QuestionnaireEntryEffect> {
  override fun init(model: QuestionnaireEntryModel): First<QuestionnaireEntryModel, QuestionnaireEntryEffect> {
    return first(model, LoadCurrentFacility, LoadQuestionnaireFormEffect(questionnaireType))
  }
}
