package org.simple.clinic.monthlyscreeningreports.list

import org.simple.clinic.mobius.ViewEffectsHandler

class MonthlyScreeningReportListViewEffectHandler(
    private val ui: MonthlyScreeningReportListUi
) : ViewEffectsHandler<MonthlyScreeningReportListViewEffect> {

  override fun handle(viewEffect: MonthlyScreeningReportListViewEffect) {
    when (viewEffect) {
      is GoBack -> ui.goBack()
      is OpenMonthlyScreeningForm -> ui.openMonthlyScreeningForm(viewEffect.questionnaireResponse)
    }
  }
}
