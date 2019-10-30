package org.simple.clinic.selectcountry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update

class SelectCountryUpdate : Update<SelectCountryModel, SelectCountryEvent, SelectCountryEffect> {

  override fun update(model: SelectCountryModel, event: SelectCountryEvent): Next<SelectCountryModel, SelectCountryEffect> {
    return when (event) {
      is ManifestFetched -> next(model.withCountries(event.countries))
    }
  }
}
