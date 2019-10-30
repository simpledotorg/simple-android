package org.simple.clinic.selectcountry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.selectcountry.SelectCountryModel.Companion.FETCHING

class SelectCountryUpdate : Update<SelectCountryModel, SelectCountryEvent, SelectCountryEffect> {

  override fun update(model: SelectCountryModel, event: SelectCountryEvent): Next<SelectCountryModel, SelectCountryEffect> {
    return when (event) {
      is ManifestFetched -> next(model.manifestFetched(event.countries))
      is ManifestFetchFailed -> next(model.manifestFetchError(event.error))
      is CountryChosen -> next(model.countryChosen(event.country))
      NextClicked -> dispatch(SaveCountryEffect(model.selectedCountry!!))
      CountrySaved -> dispatch(GoToNextScreen)
      RetryClicked -> next(FETCHING, setOf(FetchManifest))
    }
  }
}
