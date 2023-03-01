package org.simple.clinic.monthlyscreeningreports.complete

import org.simple.clinic.widgets.UiEvent

sealed class MonthlyScreeningReportCompleteEvent : UiEvent

object DoneButtonClicked : MonthlyScreeningReportCompleteEvent() {
  override val analyticsName = "Monthly Screening Reports Complete:Done Clicked"
}

