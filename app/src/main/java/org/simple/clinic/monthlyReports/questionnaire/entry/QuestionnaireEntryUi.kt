package org.simple.clinic.monthlyReports.questionnaire.entry

import org.simple.clinic.monthlyReports.questionnaire.sync.QuestionnaireLayout

interface QuestionnaireEntryUi {
  fun setFacility(facilityName: String)
  fun displayQuestionnaireFormLayout(layout: QuestionnaireLayout)
  fun goBack()
  fun showProgress()
  fun hideProgress()
}
