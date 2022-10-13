package org.simple.clinic.patient.download.formatdialog

import org.simple.clinic.patient.download.PatientLineListFileFormat
import org.simple.clinic.widgets.UiEvent

sealed class SelectPatientLineListDownloadFormatEvent : UiEvent

object DownloadButtonClicked : SelectPatientLineListDownloadFormatEvent() {

  override val analyticsName = "Select Patient Line List Download Format:Download Button Clicked"
}

data class DownloadFileFormatChanged(val fileFormat: PatientLineListFileFormat) : SelectPatientLineListDownloadFormatEvent()
