package org.simple.clinic.home.report

interface ReportsUi {
  fun showReport(html: String)
  fun showNoReportsAvailable()
}