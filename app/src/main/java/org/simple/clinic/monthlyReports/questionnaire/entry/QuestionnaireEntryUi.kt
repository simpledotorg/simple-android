package org.simple.clinic.monthlyReports.questionnaire.entry

import org.simple.clinic.monthlyReports.questionnaire.component.BaseComponentData

interface QuestionnaireEntryUi {
  fun displayQuestionnaireFormLayout(layout: BaseComponentData)
  fun goBack()
}
