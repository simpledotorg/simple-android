package org.simple.clinic.selectcountry

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class SelectCountryInit : Init<SelectCountryModel, SelectCountryEffect> {

  override fun init(model: SelectCountryModel): First<SelectCountryModel, SelectCountryEffect> {
    val effects: Set<SelectCountryEffect> = if (!model.hasFetchedCountries()) setOf(FetchManifest) else emptySet()

    return if (effects.isEmpty()) first(model) else first(model, effects)
  }
}
