package org.simple.clinic.patient.download

enum class PatientLineListFileFormat(val mimeType: String) {
  CSV(mimeType = "text/csv"),
  PDF(mimeType = "application/pdf")
}
