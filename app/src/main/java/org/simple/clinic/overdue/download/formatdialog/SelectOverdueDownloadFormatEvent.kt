package org.simple.clinic.overdue.download.formatdialog

import org.simple.clinic.overdue.download.OverdueListDownloadResult
import org.simple.clinic.overdue.download.OverdueListFileFormat
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class SelectOverdueDownloadFormatEvent : UiEvent

data class FileDownloadedForSharing(val result: OverdueListDownloadResult) : SelectOverdueDownloadFormatEvent()

data object DownloadOrShareClicked : SelectOverdueDownloadFormatEvent() {

  override val analyticsName = "Select Overdue Download Format : Download or share clicked"
}

data object OverdueDownloadScheduled : SelectOverdueDownloadFormatEvent()

data object CancelClicked : SelectOverdueDownloadFormatEvent() {

  override val analyticsName = "Select Overdue Download Format : Cancel clicked"
}

data class DownloadFormatChanged(val fileFormat: OverdueListFileFormat) : SelectOverdueDownloadFormatEvent() {

  override val analyticsName = "Select Overdue Download Format : File format changed"
}

data class SelectedOverdueAppointmentsLoaded(val selectedAppointmentIds: Set<UUID>) : SelectOverdueDownloadFormatEvent()
