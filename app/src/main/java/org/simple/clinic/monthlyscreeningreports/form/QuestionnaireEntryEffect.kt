package org.simple.clinic.monthlyscreeningreports.form

import org.simple.clinic.questionnaire.QuestionnaireType

sealed class QuestionnaireEntryEffect

object LoadCurrentFacility : QuestionnaireEntryEffect()

data class LoadQuestionnaireFormEffect(val questionnaireType: QuestionnaireType) : QuestionnaireEntryEffect()

sealed class QuestionnaireEntryViewEffect : QuestionnaireEntryEffect()

object GoBack : QuestionnaireEntryViewEffect()
