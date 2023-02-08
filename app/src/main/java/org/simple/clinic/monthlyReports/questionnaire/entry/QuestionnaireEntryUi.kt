package org.simple.clinic.monthlyReports.questionnaire.entry

import org.simple.clinic.monthlyReports.questionnaire.component.BaseComponentData

interface QuestionnaireEntryUi {
  fun setFacility(facilityName: String)
  fun displayQuestionnaireFormLayout(layout: BaseComponentData)
  fun goBack()
  fun showProgress()
  fun hideProgress()
}
