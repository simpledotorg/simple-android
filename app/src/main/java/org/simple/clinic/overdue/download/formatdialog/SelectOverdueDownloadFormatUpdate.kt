package org.simple.clinic.overdue.download.formatdialog

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class SelectOverdueDownloadFormatUpdate : Update<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEvent, SelectOverdueDownloadFormatEffect> {

  override fun update(
      model: SelectOverdueDownloadFormatModel,
      event: SelectOverdueDownloadFormatEvent
  ): Next<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEffect> {
    return when (event) {
      DownloadOrShareClicked -> downloadOrShareClicked(model)
      is FileDownloadedForSharing -> next(
          model.overdueDownloadCompleted(),
          ShareDownloadedFile(event.uri, model.overdueListFileFormat.mimeType)
      )
      OverdueDownloadScheduled, CancelClicked -> dispatch(Dismiss)
      is DownloadFormatChanged -> next(model.overdueListDownloadFormatUpdated(event.fileFormat))
    }
  }

  private fun downloadOrShareClicked(model: SelectOverdueDownloadFormatModel): Next<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEffect> {
    return when (model.openAs) {
      Share -> next(
          model.overdueDownloadInProgress(),
          DownloadForShare(model.overdueListFileFormat)
      )
      Download -> dispatch(ScheduleDownload(model.overdueListFileFormat))
      ProgressForSharing -> noChange()
    }
  }
}
