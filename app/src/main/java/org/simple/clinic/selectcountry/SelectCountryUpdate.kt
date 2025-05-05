package org.simple.clinic.selectcountry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.appconfig.Country
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class SelectCountryUpdate : Update<SelectCountryModel, SelectCountryEvent, SelectCountryEffect> {

  override fun update(
      model: SelectCountryModel,
      event: SelectCountryEvent
  ): Next<SelectCountryModel, SelectCountryEffect> {
    return when (event) {
      is ManifestFetched -> manifestFetched(model, event.countries)
      is ManifestFetchFailed -> next(model.manifestFetchError(event.error))
      is CountryChosen -> next(model.countryChosen(event.country), SaveCountryEffect(event.country))
      CountrySaved -> countrySaved(model)
      RetryClicked -> next(model.fetching(), FetchManifest)
    }
  }

  private fun countrySaved(model: SelectCountryModel): Next<SelectCountryModel, SelectCountryEffect> {
    val effect = if (model.hasMoreThanOneCountry) {
      GoToStateSelectionScreen
    } else {
      ReplaceWithStateSelectionScreen
    }
    return dispatch(effect)
  }

  private fun manifestFetched(model: SelectCountryModel, countries: List<Country>): Next<SelectCountryModel, SelectCountryEffect> {
    val manifestFetchedModel = model.manifestFetched(countries)
    return if (countries.size > 1) {
      next(manifestFetchedModel)
    } else {
      next(manifestFetchedModel, SaveCountryEffect(countries.first()))
    }
  }
}
