package org.simple.clinic.monthlyreports.list

import org.simple.clinic.mobius.ViewEffectsHandler

class MonthlyReportsViewEffectHandler(
    private val ui: MonthlyReportsUi
) : ViewEffectsHandler<MonthlyReportsViewEffect> {

  override fun handle(viewEffect: MonthlyReportsViewEffect) {
    when (viewEffect) {
      is GoBack -> ui.goBack()
      is OpenMonthlyReportForm -> ui.openMonthlyReportForm(viewEffect.questionnaireType, viewEffect.questionnaireResponse)
    }
  }
}
