package org.simple.clinic.overdue.download

enum class OverdueListDownloadFormat(val mimeType: String) {
  CSV(mimeType = "text/csv"), PDF(mimeType = "application/pdf")
}
