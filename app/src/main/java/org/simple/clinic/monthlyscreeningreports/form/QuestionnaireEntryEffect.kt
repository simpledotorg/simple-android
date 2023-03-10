package org.simple.clinic.monthlyscreeningreports.form

import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import java.util.UUID

sealed class QuestionnaireEntryEffect

object LoadCurrentFacility : QuestionnaireEntryEffect()

data class LoadQuestionnaireFormEffect(val questionnaireType: QuestionnaireType) : QuestionnaireEntryEffect()

data class LoadQuestionnaireResponseEffect(val questionnaireResponseId: UUID) : QuestionnaireEntryEffect()

data class SaveQuestionnaireResponseEffect(val questionnaireResponse: QuestionnaireResponse) : QuestionnaireEntryEffect()

sealed class QuestionnaireEntryViewEffect : QuestionnaireEntryEffect()

object GoBack : QuestionnaireEntryViewEffect()

object ShowUnsavedChangesWarningDialog : QuestionnaireEntryViewEffect()

object GoToMonthlyScreeningReportsCompleteScreen : QuestionnaireEntryViewEffect()
