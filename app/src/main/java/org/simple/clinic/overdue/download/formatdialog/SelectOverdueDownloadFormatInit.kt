package org.simple.clinic.overdue.download.formatdialog

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first
import org.simple.clinic.overdue.download.OverdueListFileFormat.CSV

class SelectOverdueDownloadFormatInit : Init<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEffect> {
  override fun init(model: SelectOverdueDownloadFormatModel): First<SelectOverdueDownloadFormatModel, SelectOverdueDownloadFormatEffect> {
    return if (model.openAs is SharingInProgress) {
      first(model.overdueDownloadInProgress(), DownloadForShare(CSV))
    } else {
      first(model)
    }
  }
}
