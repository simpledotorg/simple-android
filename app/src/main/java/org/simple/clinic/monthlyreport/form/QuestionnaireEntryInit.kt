package org.simple.clinic.monthlyreport.form

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import org.simple.clinic.questionnaire.QuestionnaireType

class QuestionnaireEntryInit(
    private val questionnaireType: QuestionnaireType,
) : Init<QuestionnaireEntryModel, QuestionnaireEntryEffect> {
  override fun init(model: QuestionnaireEntryModel): First<QuestionnaireEntryModel, QuestionnaireEntryEffect> {
    return first(model,
        LoadCurrentFacility,
        LoadQuestionnaireFormEffect(questionnaireType)
    )
  }
}
