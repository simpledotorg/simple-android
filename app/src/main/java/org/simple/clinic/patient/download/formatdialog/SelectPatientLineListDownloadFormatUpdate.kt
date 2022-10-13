package org.simple.clinic.patient.download.formatdialog

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class SelectPatientLineListDownloadFormatUpdate : Update<
    SelectPatientLineListDownloadFormatModel,
    SelectPatientLineListDownloadFormatEvent,
    PatientLineListDownloadFormatEffect> {

  override fun update(
      model: SelectPatientLineListDownloadFormatModel,
      event: SelectPatientLineListDownloadFormatEvent
  ): Next<SelectPatientLineListDownloadFormatModel, PatientLineListDownloadFormatEffect> {
    return when (event) {
      DownloadButtonClicked -> dispatch(SchedulePatientLineListDownload(model.fileFormat))
      is DownloadFileFormatChanged -> next(model.fileFormatChanged(event.fileFormat))
    }
  }
}
