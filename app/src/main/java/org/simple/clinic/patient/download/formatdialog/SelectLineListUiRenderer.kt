package org.simple.clinic.patient.download.formatdialog

import org.simple.clinic.mobius.ViewRenderer

class SelectLineListUiRenderer(private val ui: SelectLineListUi) : ViewRenderer<SelectLineListFormatModel> {

  override fun render(model: SelectLineListFormatModel) {
    ui.setLineListFormat(model.fileFormat)
  }
}
