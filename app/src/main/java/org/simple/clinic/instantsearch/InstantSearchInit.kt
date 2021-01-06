package org.simple.clinic.instantsearch

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class InstantSearchInit : Init<InstantSearchModel, InstantSearchEffect> {

  override fun init(model: InstantSearchModel): First<InstantSearchModel, InstantSearchEffect> {
    val effects = mutableSetOf<InstantSearchEffect>()

    if (model.hasAdditionalIdentifier)
      effects.add(OpenBpPassportSheet(model.additionalIdentifier!!))

    if (model.hasFacility)
      effects.add(ValidateSearchQuery(model.searchQuery.orEmpty()))
    else
      effects.add(LoadCurrentFacility)

    return first(model, effects)
  }
}
