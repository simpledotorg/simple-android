package org.simple.clinic.instantsearch

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class InstantSearchInit : Init<InstantSearchModel, InstantSearchEffect> {

  override fun init(model: InstantSearchModel): First<InstantSearchModel, InstantSearchEffect> {
    var modelToEmit = model
    val effects = mutableSetOf<InstantSearchEffect>()

    if (modelToEmit.hasAdditionalIdentifier && !modelToEmit.bpPassportSheetAlreadyOpened) {
      effects.add(OpenBpPassportSheet(modelToEmit.additionalIdentifier!!))
      modelToEmit = modelToEmit.bpPassportSheetOpened()
    }

    if (modelToEmit.hasFacility)
      effects.add(ValidateSearchQuery(modelToEmit.searchQuery.orEmpty()))
    else
      effects.add(LoadCurrentFacility)

    return first(modelToEmit, effects)
  }
}
