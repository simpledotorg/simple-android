package org.simple.clinic.selectcountry

import org.simple.clinic.mobius.ViewRenderer

class SelectCountryUiRenderer(private val ui: SelectCountryUi) : ViewRenderer<SelectCountryModel> {

  override fun render(model: SelectCountryModel) {
    when {
      model.hasFetchedCountries() -> {
        ui.hideProgress()
        ui.displaySupportedCountries(model.countries!!)
      }
      model.isFetching() -> ui.showProgress()
      model.hasFailedToFetchCountries() -> {
        ui.hideProgress()
        when(model.manifestFetchError!!) {
          NetworkError -> ui.displayNetworkErrorMessage()
          ServerError -> ui.displayServerErrorMessage()
          UnexpectedError -> ui.displayGenericErrorMessage()
        }
        ui.showRetryButton()
      }
    }
  }
}
