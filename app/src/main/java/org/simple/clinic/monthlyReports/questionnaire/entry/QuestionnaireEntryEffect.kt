package org.simple.clinic.monthlyReports.questionnaire.entry

import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireType

sealed class QuestionnaireEntryEffect

data class LoadQuestionnaireFormEffect(val questionnaireType: QuestionnaireType) : QuestionnaireEntryEffect()

sealed class QuestionnaireEntryViewEffect : QuestionnaireEntryEffect()

object GoBack : QuestionnaireEntryViewEffect()
