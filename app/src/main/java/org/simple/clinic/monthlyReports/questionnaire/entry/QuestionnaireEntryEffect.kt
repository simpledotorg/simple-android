package org.simple.clinic.monthlyReports.questionnaire.entry

import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireType

sealed class QuestionnaireEntryEffect

object LoadCurrentFacility : QuestionnaireEntryEffect()

data class LoadQuestionnaireFormEffect(val questionnaireType: QuestionnaireType) : QuestionnaireEntryEffect()

sealed class QuestionnaireEntryViewEffect : QuestionnaireEntryEffect()

object GoBack : QuestionnaireEntryViewEffect()
