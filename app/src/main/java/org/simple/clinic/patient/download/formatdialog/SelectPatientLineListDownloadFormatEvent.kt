package org.simple.clinic.patient.download.formatdialog

import org.simple.clinic.widgets.UiEvent

sealed class SelectPatientLineListDownloadFormatEvent : UiEvent

object DownloadButtonClicked : SelectPatientLineListDownloadFormatEvent() {

  override val analyticsName = "Select Patient Line List Download Format:Download Button Clicked"
}
