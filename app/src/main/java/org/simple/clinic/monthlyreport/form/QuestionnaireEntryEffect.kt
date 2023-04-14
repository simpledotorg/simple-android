package org.simple.clinic.monthlyreport.form

import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

sealed class QuestionnaireEntryEffect

object LoadCurrentFacility : QuestionnaireEntryEffect()

data class LoadQuestionnaireFormEffect(val questionnaireType: QuestionnaireType) : QuestionnaireEntryEffect()

data class SaveQuestionnaireResponseEffect(val questionnaireResponse: QuestionnaireResponse) : QuestionnaireEntryEffect()

sealed class QuestionnaireEntryViewEffect : QuestionnaireEntryEffect()

object GoBack : QuestionnaireEntryViewEffect()

object ShowUnsavedChangesWarningDialog : QuestionnaireEntryViewEffect()

object GoToMonthlyScreeningReportsCompleteScreen : QuestionnaireEntryViewEffect()
