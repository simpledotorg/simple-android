package org.simple.clinic.monthlyreport.complete

import org.simple.clinic.mobius.ViewEffectsHandler

class MonthlyReportCompleteViewEffectHandler(
    private val ui: MonthlyReportCompleteUi
) : ViewEffectsHandler<MonthlyReportCompleteViewEffect> {

  override fun handle(viewEffect: MonthlyReportCompleteViewEffect) {
    when (viewEffect) {
      GoToMonthlyReportListScreen -> ui.goToMonthlyReportListScreen()
    }
  }
}
