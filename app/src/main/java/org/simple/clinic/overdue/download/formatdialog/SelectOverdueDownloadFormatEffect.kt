package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri
import org.simple.clinic.overdue.download.OverdueListDownloadFormat

sealed class SelectOverdueDownloadFormatEffect

data class DownloadForShare(val downloadFormat: OverdueListDownloadFormat) : SelectOverdueDownloadFormatEffect()

data class ScheduleDownload(val downloadFormat: OverdueListDownloadFormat) : SelectOverdueDownloadFormatEffect()

sealed class SelectOverdueDownloadFormatViewEffect : SelectOverdueDownloadFormatEffect()

data class ShareDownloadedFile(val uri: Uri) : SelectOverdueDownloadFormatViewEffect()

object Dismiss : SelectOverdueDownloadFormatViewEffect()
