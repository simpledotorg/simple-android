package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri

sealed class SelectOverdueDownloadFormatEvent

data class FileDownloadedForSharing(val uri: Uri) : SelectOverdueDownloadFormatEvent()

object DownloadOrShareClicked : SelectOverdueDownloadFormatEvent()

object OverdueDownloadScheduled : SelectOverdueDownloadFormatEvent()

object CancelClicked : SelectOverdueDownloadFormatEvent()
