package org.simple.clinic.instantsearch

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class InstantSearchInit : Init<InstantSearchModel, InstantSearchEffect> {

  override fun init(model: InstantSearchModel): First<InstantSearchModel, InstantSearchEffect> {
    return first(model, LoadCurrentFacility)
  }
}
