package org.simple.clinic.monthlyReports.questionnaire.entry

import org.simple.clinic.mobius.ViewEffectsHandler

class QuestionnaireEntryViewEffectHandler(
    private val ui: QuestionnaireEntryUi
) : ViewEffectsHandler<QuestionnaireEntryViewEffect> {

  override fun handle(viewEffect: QuestionnaireEntryViewEffect) {
    when (viewEffect) {
      is GoBack -> ui.goBack()
    }
  }
}
