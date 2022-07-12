package org.simple.clinic.overdue.download.formatdialog

import android.net.Uri
import org.simple.clinic.overdue.download.OverdueListFileFormat
import java.util.UUID

sealed class SelectOverdueDownloadFormatEffect

data class DownloadForShare(val fileFormat: OverdueListFileFormat, val selectedAppointmentIds: Set<UUID>) : SelectOverdueDownloadFormatEffect()

data class ScheduleDownload(val fileFormat: OverdueListFileFormat, val selectedAppointmentIds: Set<UUID>) : SelectOverdueDownloadFormatEffect()

sealed class SelectOverdueDownloadFormatViewEffect : SelectOverdueDownloadFormatEffect()

data class ShareDownloadedFile(val uri: Uri, val mimeType: String) : SelectOverdueDownloadFormatViewEffect()

object Dismiss : SelectOverdueDownloadFormatViewEffect()

object OpenNotEnoughStorageErrorDialog : SelectOverdueDownloadFormatViewEffect()

object OpenDownloadFailedErrorDialog : SelectOverdueDownloadFormatViewEffect()
