package org.simple.clinic.monthlyreports.complete

import org.simple.clinic.mobius.ViewEffectsHandler

class MonthlyReportCompleteViewEffectHandler(
    private val ui: MonthlyReportCompleteUi
) : ViewEffectsHandler<MonthlyReportCompleteViewEffect> {

  override fun handle(viewEffect: MonthlyReportCompleteViewEffect) {
    when (viewEffect) {
      GoToMonthlyReportsScreen -> ui.goToMonthlyReportsScreen()
    }
  }
}
