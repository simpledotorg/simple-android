package org.simple.clinic.overdue.download.formatdialog

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.exhaustive

class SelectOverdueDownloadFormatUiRenderer(
    private val ui: SelectOverdueDownloadFormatUi
) : ViewRenderer<SelectOverdueDownloadFormatModel> {

  override fun render(model: SelectOverdueDownloadFormatModel) {
    ui.setOverdueListFormat(model.overdueListDownloadFormat)

    when (model.openAs) {
      Download -> renderDownloadUi()
      Share -> {
        // look away, nothing to see here
      }
    }.exhaustive()
  }

  private fun renderDownloadUi() {
    ui.setDownloadTitle()
    ui.setDownloadButtonLabel()
  }
}
