package org.simple.clinic.instantsearch

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class InstantSearchInit : Init<InstantSearchModel, InstantSearchEffect> {

  override fun init(model: InstantSearchModel): First<InstantSearchModel, InstantSearchEffect> {
    var modelToEmit = model
    val effects = mutableSetOf<InstantSearchEffect>()

    if (modelToEmit.hasAdditionalIdentifier && !modelToEmit.scannedQrCodeSheetAlreadyOpened) {
      effects.add(OpenScannedQrCodeSheet(modelToEmit.additionalIdentifier!!))
      modelToEmit = modelToEmit.scannedQrCodeSheetOpened()
    }

    if (!modelToEmit.hasFacility) effects.add(LoadCurrentFacility)

    if (modelToEmit.hasSearchQuery) effects.add(ValidateSearchQuery(model.searchQuery!!))

    return first(modelToEmit, effects)
  }
}
