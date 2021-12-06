package org.simple.clinic.overdue.download.formatdialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.overdue.download.OverdueListFileFormat

@Parcelize
data class SelectOverdueDownloadFormatModel(
    val openAs: OpenAs,
    val overdueListFileFormat: OverdueListFileFormat,
    val overdueDownloadStatus: OverdueDownloadStatus?
) : Parcelable {

  companion object {

    fun create(openAs: OpenAs) = SelectOverdueDownloadFormatModel(
        openAs = openAs,
        overdueListFileFormat = OverdueListFileFormat.PDF,
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
