package org.simple.clinic.overdue.download.formatdialog

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.overdue.download.OverdueListDownloadResult

class SelectOverdueDownloadFormatUpdate : Update<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEvent, SelectOverdueDownloadFormatEffect> {

  override fun update(
      model: SelectOverdueDownloadFormatModel,
      event: SelectOverdueDownloadFormatEvent
  ): Next<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEffect> {
    return when (event) {
      DownloadOrShareClicked -> downloadOrShareClicked(model)
      is FileDownloadedForSharing -> fileDownloadedForSharing(model, event)
      OverdueDownloadScheduled, CancelClicked -> dispatch(Dismiss)
      is DownloadFormatChanged -> next(model.overdueListDownloadFormatUpdated(event.fileFormat))
    }
  }

  private fun fileDownloadedForSharing(
      model: SelectOverdueDownloadFormatModel,
      event: FileDownloadedForSharing
  ): Next<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEffect> {
    return when (val result = event.result) {
      is OverdueListDownloadResult.DownloadSuccessful -> next(
          model.overdueDownloadCompleted(),
          ShareDownloadedFile(result.uri, model.overdueListFileFormat.mimeType)
      )
      OverdueListDownloadResult.DownloadFailed -> dispatch(OpenDownloadFailedErrorDialog)
      OverdueListDownloadResult.NotEnoughStorage -> dispatch(OpenNotEnoughStorageErrorDialog)
    }
  }

  private fun downloadOrShareClicked(model: SelectOverdueDownloadFormatModel): Next<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEffect> {
    return when (model.openAs) {
      Share -> next(
          model.overdueDownloadInProgress(),
          DownloadForShare(model.overdueListFileFormat)
      )
      Download -> dispatch(ScheduleDownload(model.overdueListFileFormat))
      SharingInProgress -> noChange()
    }
  }
}
