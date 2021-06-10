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

    if (modelToEmit.hasFacility)
      effects.add(PrefillSearchQuery(modelToEmit.searchQuery.orEmpty()))
    else
      effects.add(LoadCurrentFacility)

    // When a scanned BP Passport does not result in a match, we bring up a bottom sheet which asks
    // whether this is a new registration or an existing patient. If we show the keyboard in these
    // cases, the UI is janky since the keyboard pops up and immediately another bottom sheet pops up.
    // This improves the experience by showing the keyboard only if we have arrived here by searching
    // for a patient by the name
    if (!modelToEmit.hasAdditionalIdentifier) {
      effects.add(ShowKeyboard)
    }

    return first(modelToEmit, effects)
  }
}
