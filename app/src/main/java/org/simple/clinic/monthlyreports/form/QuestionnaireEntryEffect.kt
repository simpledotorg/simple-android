package org.simple.clinic.monthlyreports.form

import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

sealed class QuestionnaireEntryEffect

data object LoadCurrentFacility : QuestionnaireEntryEffect()

data class LoadQuestionnaireFormEffect(val questionnaireType: QuestionnaireType) : QuestionnaireEntryEffect()

data class SaveQuestionnaireResponseEffect(val questionnaireResponse: QuestionnaireResponse) : QuestionnaireEntryEffect()

sealed class QuestionnaireEntryViewEffect : QuestionnaireEntryEffect()

data object GoBack : QuestionnaireEntryViewEffect()

data object ShowUnsavedChangesWarningDialog : QuestionnaireEntryViewEffect()

data object GoToMonthlyReportsCompleteScreen : QuestionnaireEntryViewEffect()
