package org.simple.clinic.instantsearch

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class InstantSearchInit : Init<InstantSearchModel, InstantSearchEffect> {

  override fun init(model: InstantSearchModel): First<InstantSearchModel, InstantSearchEffect> {
    val effect = if (model.hasFacility)
      ValidateSearchQuery(model.searchQuery.orEmpty())
    else
      LoadCurrentFacility

    return first(model, effect)
  }
}
