package org.simple.clinic.selectcountry

import org.simple.clinic.mobius.ViewRenderer

class SelectCountryUiRenderer(private val ui: SelectCountryUi) : ViewRenderer<SelectCountryModel> {

  override fun render(model: SelectCountryModel) {
    when {
      model.hasFetchedCountries() -> {
        ui.displaySupportedCountries(model.countries!!, model.selectedCountry)
        if (model.hasSelectedACountry()) {
          ui.showNextButton()
        }
      }
      model.isFetching() -> {
        ui.showProgress()
      }
      model.hasFailedToFetchCountries() -> {
        when (model.manifestFetchError!!) {
          NetworkError -> ui.displayNetworkErrorMessage()
          ServerError -> ui.displayServerErrorMessage()
          UnexpectedError -> ui.displayGenericErrorMessage()
        }
      }
    }
  }
}
