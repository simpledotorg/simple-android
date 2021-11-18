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
      Share -> renderShareUi()
    }.exhaustive()
  }

  private fun renderShareUi() {
    ui.setShareTitle()
    ui.setShareButtonLabel()
  }

  private fun renderDownloadUi() {
    ui.setDownloadTitle()
    ui.setDownloadButtonLabel()
  }
}
