package org.simple.clinic.monthlyscreeningreports.form

import org.simple.clinic.questionnaire.component.BaseComponentData
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse

interface QuestionnaireEntryUi {
  fun setFacility(facilityName: String)
  fun setToolbarMonth(response: QuestionnaireResponse)
  fun setSubmittedView(response: QuestionnaireResponse)
  fun displayQuestionnaireFormLayout(layout: BaseComponentData, response: QuestionnaireResponse)
  fun goBack()
  fun showUnsavedChangesWarningDialog()
  fun goToMonthlyScreeningReportsCompleteScreen()
  fun showProgress()
  fun hideProgress()
}
