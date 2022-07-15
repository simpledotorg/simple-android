package org.simple.clinic.overdue.download.formatdialog

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class SelectOverdueDownloadFormatInit : Init<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEffect> {
  override fun init(model: SelectOverdueDownloadFormatModel): First<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEffect> {
    return if (model.openAs is SharingInProgress) {
      first(model.overdueDownloadInProgress(), LoadSelectedOverdueAppointmentIds)
    } else {
      first(model)
    }
  }
}
