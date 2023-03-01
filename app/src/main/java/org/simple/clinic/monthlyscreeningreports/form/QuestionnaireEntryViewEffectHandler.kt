package org.simple.clinic.monthlyscreeningreports.form

import org.simple.clinic.mobius.ViewEffectsHandler

class QuestionnaireEntryViewEffectHandler(
    private val ui: QuestionnaireEntryUi
) : ViewEffectsHandler<QuestionnaireEntryViewEffect> {

  override fun handle(viewEffect: QuestionnaireEntryViewEffect) {
    when (viewEffect) {
      is GoBack -> ui.goBack()
      is ShowUnsavedChangesWarningDialog -> ui.showUnsavedChangesWarningDialog()
      is GoToMonthlyReportsCompleteScreen -> ui.goToMonthlyReportsCompleteScreen()
    }
  }
}
