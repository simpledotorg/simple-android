package org.simple.clinic.overdue.download.formatdialog

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class SelectOverdueDownloadFormatViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<SelectOverdueDownloadFormatViewEffect> {

  override fun handle(viewEffect: SelectOverdueDownloadFormatViewEffect) {
    when (viewEffect) {
      is ShareDownloadedFile -> uiActions.shareDownloadedFile(viewEffect.uri, viewEffect.mimeType)
      Dismiss -> uiActions.dismiss()
      OpenNotEnoughStorageErrorDialog -> uiActions.openNotEnoughStorageErrorDialog()
      OpenDownloadFailedErrorDialog -> uiActions.openDownloadFailedErrorDialog()
    }.exhaustive()
  }
}
