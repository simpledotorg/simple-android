package org.simple.clinic.overdue.download.formatdialog

import org.simple.clinic.overdue.download.OverdueListDownloadFormat

sealed class SelectOverdueDownloadFormatEffect

data class DownloadForShare(val downloadFormat: OverdueListDownloadFormat) : SelectOverdueDownloadFormatEffect()

sealed class SelectOverdueDownloadFormatViewEffect : SelectOverdueDownloadFormatEffect()
