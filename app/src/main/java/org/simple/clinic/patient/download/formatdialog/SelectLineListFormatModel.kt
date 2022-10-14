package org.simple.clinic.patient.download.formatdialog

import org.simple.clinic.patient.download.PatientLineListFileFormat

data class SelectLineListFormatModel(
    val fileFormat: PatientLineListFileFormat
) {
  companion object {

    fun create() = SelectLineListFormatModel(
        fileFormat = PatientLineListFileFormat.PDF
    )
  }

  fun fileFormatChanged(fileFormat: PatientLineListFileFormat): SelectLineListFormatModel {
    return copy(fileFormat = fileFormat)
  }
}
