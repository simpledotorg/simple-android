package org.simple.clinic.monthlyreport.complete

import org.simple.clinic.mobius.ViewEffectsHandler

class MonthlyScreeningReportCompleteViewEffectHandler(
    private val ui: MonthlyScreeningReportCompleteUi
) : ViewEffectsHandler<MonthlyScreeningReportCompleteViewEffect> {

  override fun handle(viewEffect: MonthlyScreeningReportCompleteViewEffect) {
    when (viewEffect) {
      GoToMonthlyScreeningReportListScreen -> ui.goToMonthlyScreeningReportListScreen()
    }
  }
}
