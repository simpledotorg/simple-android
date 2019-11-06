package org.simple.clinic.selectcountry

import org.simple.clinic.mobius.ViewRenderer

class SelectCountryUiRenderer(private val ui: SelectCountryUi) : ViewRenderer<SelectCountryModel> {

  override fun render(model: SelectCountryModel) {
    ui.showProgress()
  }
}
