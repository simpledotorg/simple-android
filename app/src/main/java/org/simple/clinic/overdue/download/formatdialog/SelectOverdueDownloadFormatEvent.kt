package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri
import org.simple.clinic.overdue.download.OverdueListFileFormat

sealed class SelectOverdueDownloadFormatEvent

data class FileDownloadedForSharing(val uri: Uri) : SelectOverdueDownloadFormatEvent()

object DownloadOrShareClicked : SelectOverdueDownloadFormatEvent()

object OverdueDownloadScheduled : SelectOverdueDownloadFormatEvent()

object CancelClicked : SelectOverdueDownloadFormatEvent()

data class DownloadFormatChanged(val fileFormat: OverdueListFileFormat) : SelectOverdueDownloadFormatEvent()
