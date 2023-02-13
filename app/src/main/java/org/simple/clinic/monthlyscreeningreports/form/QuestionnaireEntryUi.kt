package org.simple.clinic.monthlyscreeningreports.form

import org.simple.clinic.questionnaire.component.BaseComponentData

interface QuestionnaireEntryUi {
  fun setFacility(facilityName: String)
  fun displayQuestionnaireFormLayout(layout: BaseComponentData)
  fun goBack()
  fun showProgress()
  fun hideProgress()
}
