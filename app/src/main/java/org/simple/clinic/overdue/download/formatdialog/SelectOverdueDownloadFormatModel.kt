package org.simple.clinic.overdue.download.formatdialog

import org.simple.clinic.overdue.download.OverdueListFileFormat

data class SelectOverdueDownloadFormatModel(
    val openAs: OpenAs,
    val overdueListFileFormat: OverdueListFileFormat,
    val overdueDownloadStatus: OverdueDownloadStatus?
) {

  companion object {

    fun create(openAs: OpenAs) = SelectOverdueDownloadFormatModel(
        openAs = openAs,
        overdueListFileFormat = OverdueListFileFormat.CSV,
        overdueDownloadStatus = null
    )
  }

  val isDownloadForShareInProgress
    get() = overdueDownloadStatus == OverdueDownloadStatus.InProgress

  fun overdueListDownloadFormatUpdated(overdueListFileFormat: OverdueListFileFormat): SelectOverdueDownloadFormatModel {
    return copy(overdueListFileFormat = overdueListFileFormat)
  }

  fun overdueDownloadInProgress(): SelectOverdueDownloadFormatModel {
    return copy(overdueDownloadStatus = OverdueDownloadStatus.InProgress)
  }

  fun overdueDownloadCompleted(): SelectOverdueDownloadFormatModel {
    return copy(overdueDownloadStatus = OverdueDownloadStatus.Done)
  }
}
