package org.simple.clinic.patient.download.formatdialog

import org.simple.clinic.patient.download.PatientLineListFileFormat

data class SelectPatientLineListDownloadFormatModel(
    val fileFormat: PatientLineListFileFormat
) {
  companion object {

    fun create() = SelectPatientLineListDownloadFormatModel(
        fileFormat = PatientLineListFileFormat.PDF
    )
  }
}
