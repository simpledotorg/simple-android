package org.simple.clinic.patient.download.formatdialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.download.PatientLineListFileFormat

@Parcelize
data class SelectLineListFormatModel(
    val fileFormat: PatientLineListFileFormat
) : Parcelable {
  companion object {

    fun create() = SelectLineListFormatModel(
        fileFormat = PatientLineListFileFormat.PDF
    )
  }

  fun fileFormatChanged(fileFormat: PatientLineListFileFormat): SelectLineListFormatModel {
    return copy(fileFormat = fileFormat)
  }
}
