package org.simple.clinic.monthlyscreeningreports.list

import java.util.UUID

sealed class MonthlyScreeningReportListEffect

object LoadCurrentFacility : MonthlyScreeningReportListEffect()

object LoadMonthlyScreeningReportListEffect : MonthlyScreeningReportListEffect()

sealed class MonthlyScreeningReportListViewEffect : MonthlyScreeningReportListEffect()

object GoBack : MonthlyScreeningReportListViewEffect()

data class OpenMonthlyScreeningForm(val uuid: UUID) : MonthlyScreeningReportListViewEffect()


