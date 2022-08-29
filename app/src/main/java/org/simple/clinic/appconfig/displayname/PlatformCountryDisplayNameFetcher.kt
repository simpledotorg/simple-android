package org.simple.clinic.appconfig.displayname

import android.content.res.Resources
import androidx.annotation.StringRes
import org.simple.clinic.R
import org.simple.clinic.appconfig.Country
import javax.inject.Inject

class PlatformCountryDisplayNameFetcher @Inject constructor(
    private val resources: Resources
) : CountryDisplayNameFetcher {

  override fun displayNameForCountry(country: Country): String {
    return when (country.isoCountryCode) {
      Country.INDIA -> string(R.string.country_india)
      Country.BANGLADESH -> string(R.string.country_bangladesh)
      Country.ETHIOPIA -> string(R.string.country_ethiopia)
      Country.SRI_LANKA -> string(R.string.country_sri_lanka)
      Country.DEMO -> string(R.string.country_demo)
      else -> country.displayName
    }
  }

  private fun string(@StringRes id: Int): String {
    return resources.getString(id)
  }
}
