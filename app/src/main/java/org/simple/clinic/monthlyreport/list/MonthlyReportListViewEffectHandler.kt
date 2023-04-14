package org.simple.clinic.monthlyreport.list

import org.simple.clinic.mobius.ViewEffectsHandler

class MonthlyReportListViewEffectHandler(
    private val ui: MonthlyReportListUi
) : ViewEffectsHandler<MonthlyReportListViewEffect> {

  override fun handle(viewEffect: MonthlyReportListViewEffect) {
    when (viewEffect) {
      is GoBack -> ui.goBack()
      is OpenMonthlyReportForm -> ui.openMonthlyReportForm(viewEffect.questionnaireType, viewEffect.questionnaireResponse)
    }
  }
}
