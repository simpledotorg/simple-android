package org.simple.clinic.monthlyreport.form

import org.simple.clinic.questionnaire.component.BaseComponentData

interface QuestionnaireEntryUi {
  fun setFacility(facilityName: String)
  fun displayQuestionnaireFormLayout(layout: BaseComponentData)
  fun goBack()
  fun showUnsavedChangesWarningDialog()
  fun goToMonthlyScreeningReportsCompleteScreen()
}
