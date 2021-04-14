package org.simple.clinic.selectcountry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appconfig.Country

@Parcelize
data class SelectCountryModel(
    val countries: List<Country>?,
    val manifestFetchError: ManifestFetchError?,
    val selectedCountry: Country?
) : Parcelable {

  fun hasFetchedCountries(): Boolean = countries != null

  fun hasFailedToFetchCountries(): Boolean = manifestFetchError != null

  fun isFetching(): Boolean = countries == null && manifestFetchError == null

  fun hasSelectedACountry(): Boolean = selectedCountry != null

  fun manifestFetched(countries: List<Country>): SelectCountryModel {
    return copy(countries = countries, manifestFetchError = null)
  }

  fun manifestFetchError(manifestFetchError: ManifestFetchError): SelectCountryModel {
    return copy(manifestFetchError = manifestFetchError, countries = null)
  }

  fun countryChosen(country: Country): SelectCountryModel {
    return copy(selectedCountry = country)
  }

  fun fetching(): SelectCountryModel {
    return copy(countries = null, manifestFetchError = null)
  }

  companion object {
    val FETCHING = SelectCountryModel(
        countries = null,
        manifestFetchError = null,
        selectedCountry = null
    )
  }
}
