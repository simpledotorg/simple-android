package org.simple.clinic.monthlyscreeningreports.form

import org.simple.clinic.mobius.ViewRenderer

class QuestionnaireEntryUiRenderer(private val ui: QuestionnaireEntryUi) : ViewRenderer<QuestionnaireEntryModel> {
  override fun render(model: QuestionnaireEntryModel) {
    val questionnaireForm = model.questionnaire

    if (questionnaireForm?.layout != null && model.questionnaireResponse != null) {
      ui.displayQuestionnaireFormLayout(questionnaireForm.layout, model.questionnaireResponse)
    } else {
      //no-op
    }
    if (model.hasFacility) {
      renderFacility(model)
    }
  }

  private fun renderFacility(model: QuestionnaireEntryModel) {
    ui.setFacility(model.facility!!.name)
  }
}
