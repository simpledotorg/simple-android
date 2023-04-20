package org.simple.clinic.monthlyreports.form

import org.simple.clinic.questionnaire.component.BaseComponentData

interface QuestionnaireEntryUi {
  fun setFacility(facilityName: String)
  fun displayQuestionnaireFormLayout(layout: BaseComponentData)
  fun goBack()
  fun showUnsavedChangesWarningDialog()
  fun goToMonthlyReportsCompleteScreen()
}
