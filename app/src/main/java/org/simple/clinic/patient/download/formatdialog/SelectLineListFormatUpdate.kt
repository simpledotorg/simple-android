package org.simple.clinic.patient.download.formatdialog

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class SelectLineListFormatUpdate : Update<
    SelectLineListFormatModel,
    SelectLineListFormatEvent,
    SelectLineListFormatEffect> {

  override fun update(
      model: SelectLineListFormatModel,
      event: SelectLineListFormatEvent
  ): Next<SelectLineListFormatModel, SelectLineListFormatEffect> {
    return when (event) {
      DownloadButtonClicked -> dispatch(SchedulePatientLineListDownload(model.fileFormat))
      is DownloadFileFormatChanged -> next(model.fileFormatChanged(event.fileFormat))
      CancelButtonClicked -> dispatch(Dismiss)
    }
  }
}
