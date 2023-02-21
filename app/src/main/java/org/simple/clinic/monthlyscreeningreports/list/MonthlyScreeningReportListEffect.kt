package org.simple.clinic.monthlyscreeningreports.list

sealed class MonthlyScreeningReportListEffect

object LoadCurrentFacility : MonthlyScreeningReportListEffect()

object LoadMonthlyReportListEffect : MonthlyScreeningReportListEffect()

sealed class MonthlyScreeningReportListViewEffect : MonthlyScreeningReportListEffect()

object GoBack : MonthlyScreeningReportListViewEffect()
