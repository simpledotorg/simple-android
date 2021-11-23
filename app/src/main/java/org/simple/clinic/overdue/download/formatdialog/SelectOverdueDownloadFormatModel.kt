package org.simple.clinic.overdue.download.formatdialog

import org.simple.clinic.overdue.download.OverdueListDownloadFormat

data class SelectOverdueDownloadFormatModel(
    val openAs: OpenAs,
    val overdueListDownloadFormat: OverdueListDownloadFormat,
    val overdueDownloadStatus: OverdueDownloadStatus?
) {

  companion object {

    fun create(openAs: OpenAs) = SelectOverdueDownloadFormatModel(
        openAs = openAs,
        overdueListDownloadFormat = OverdueListDownloadFormat.CSV,
        overdueDownloadStatus = null
    )
  }

  fun overdueListDownloadFormatUpdated(overdueListDownloadFormat: OverdueListDownloadFormat): SelectOverdueDownloadFormatModel {
    return copy(overdueListDownloadFormat = overdueListDownloadFormat)
  }

  fun overdueDownloadInProgress(): SelectOverdueDownloadFormatModel {
    return copy(overdueDownloadStatus = OverdueDownloadStatus.InProgress)
  }

  fun overdueDownloadCompleted(): SelectOverdueDownloadFormatModel {
    return copy(overdueDownloadStatus = OverdueDownloadStatus.Done)
  }
}
