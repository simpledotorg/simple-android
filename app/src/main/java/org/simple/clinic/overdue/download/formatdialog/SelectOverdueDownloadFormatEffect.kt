package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri
import org.simple.clinic.overdue.download.OverdueListFileFormat

sealed class SelectOverdueDownloadFormatEffect

data class DownloadForShare(val fileFormat: OverdueListFileFormat) : SelectOverdueDownloadFormatEffect()

data class ScheduleDownload(val fileFormat: OverdueListFileFormat) : SelectOverdueDownloadFormatEffect()

sealed class SelectOverdueDownloadFormatViewEffect : SelectOverdueDownloadFormatEffect()

data class ShareDownloadedFile(val uri: Uri, val mimeType: String) : SelectOverdueDownloadFormatViewEffect()

object Dismiss : SelectOverdueDownloadFormatViewEffect()

object OpenNotEnoughStorageErrorDialog : SelectOverdueDownloadFormatViewEffect()

object OpenDownloadFailedErrorDialog : SelectOverdueDownloadFormatViewEffect()
