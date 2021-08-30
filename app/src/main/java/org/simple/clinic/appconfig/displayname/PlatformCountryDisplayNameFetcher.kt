package org.simple.clinic.appconfig.displayname

import android.content.res.Resources
import androidx.annotation.StringRes
import org.simple.clinic.R
import org.simple.clinic.appconfig.CountryV2
import javax.inject.Inject

class PlatformCountryDisplayNameFetcher @Inject constructor(
    private val resources: Resources
) : CountryDisplayNameFetcher {

  override fun displayNameForCountry(country: CountryV2): String {
    return when (country.isoCountryCode) {
      CountryV2.INDIA -> string(R.string.country_india)
      CountryV2.BANGLADESH -> string(R.string.country_bangladesh)
      CountryV2.ETHIOPIA -> string(R.string.country_ethiopia)
      CountryV2.SRI_LANKA -> string(R.string.country_sri_lanka)
      else -> country.displayName
    }
  }

  private fun string(@StringRes id: Int): String {
    return resources.getString(id)
  }
}
