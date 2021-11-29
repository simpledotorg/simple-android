package org.simple.clinic.overdue.download.formatdialog

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.exhaustive

class SelectOverdueDownloadFormatUiRenderer(
    private val ui: SelectOverdueDownloadFormatUi
) : ViewRenderer<SelectOverdueDownloadFormatModel> {

  override fun render(model: SelectOverdueDownloadFormatModel) {
    if (model.isDownloadForShareInProgress) {
      renderDownloadForShareInProgress()
    } else {
      renderDialogUi(model)
    }
  }

  private fun renderDownloadForShareInProgress() {
    ui.hideTitle()
    ui.hideContent()
    ui.hideDownloadOrShareButton()
    ui.showProgress()
  }

  private fun renderDialogUi(model: SelectOverdueDownloadFormatModel) {
    ui.showTitle()
    ui.showContent()
    ui.showDownloadOrShareButton()
    ui.hideProgress()
    ui.setOverdueListFormat(model.overdueListFileFormat)

    when (model.openAs) {
      Download -> renderDownloadUi()
      Share -> renderShareUi()
      SharingInProgress -> { // do nothing
      }
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
