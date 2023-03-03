package org.simple.clinic.monthlyscreeningreports.form

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import org.simple.clinic.questionnaire.QuestionnaireType
import java.util.UUID

class QuestionnaireEntryInit(
    private val questionnaireType: QuestionnaireType,
    private val questionnaireResponseId: UUID
) : Init<QuestionnaireEntryModel, QuestionnaireEntryEffect> {
  override fun init(model: QuestionnaireEntryModel): First<QuestionnaireEntryModel, QuestionnaireEntryEffect> {
    return first(model,
        LoadCurrentFacility,
        LoadQuestionnaireFormEffect(questionnaireType),
        LoadQuestionnaireResponseEffect(questionnaireResponseId)
    )
  }
}
