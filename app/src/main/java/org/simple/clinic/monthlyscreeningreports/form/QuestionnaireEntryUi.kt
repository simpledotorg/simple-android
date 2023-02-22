package org.simple.clinic.monthlyscreeningreports.form

import org.simple.clinic.questionnaire.component.BaseComponentData
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

interface QuestionnaireEntryUi {
  fun setFacility(facilityName: String)
  fun displayQuestionnaireFormLayout(layout: BaseComponentData)
  fun displayQuestionnaireResponse(questionnaireResponse: QuestionnaireResponse)
  fun goBack()
  fun showUnsavedChangesWarningDialog()
  fun goToMonthlyReportsCompleteScreen()
  fun showProgress()
  fun hideProgress()
}
