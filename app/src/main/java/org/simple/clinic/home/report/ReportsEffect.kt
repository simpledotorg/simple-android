package org.simple.clinic.home.report

sealed class ReportsEffect

data object LoadReports : ReportsEffect()
