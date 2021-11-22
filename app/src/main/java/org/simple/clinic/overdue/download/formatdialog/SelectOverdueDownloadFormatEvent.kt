package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri
import org.simple.clinic.overdue.download.OverdueListFileFormat
import org.simple.clinic.widgets.UiEvent

sealed class SelectOverdueDownloadFormatEvent : UiEvent

data class FileDownloadedForSharing(val uri: Uri) : SelectOverdueDownloadFormatEvent()

object DownloadOrShareClicked : SelectOverdueDownloadFormatEvent() {

  override val analyticsName = "Select Overdue Download Format : Download or share clicked"
}

object OverdueDownloadScheduled : SelectOverdueDownloadFormatEvent()

object CancelClicked : SelectOverdueDownloadFormatEvent() {

  override val analyticsName = "Select Overdue Download Format : Cancel clicked"
}

data class DownloadFormatChanged(val fileFormat: OverdueListFileFormat) : SelectOverdueDownloadFormatEvent() {

  override val analyticsName = "Select Overdue Download Format : File format changed"
}
