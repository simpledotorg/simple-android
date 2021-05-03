package org.simple.clinic.shortcodesearchresult

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class ShortCodeSearchResultInit : Init<IdentifierSearchResultState, IdentifierSearchResultEffect> {

  override fun init(model: IdentifierSearchResultState): First<IdentifierSearchResultState, IdentifierSearchResultEffect> {
    return first(model, SearchByShortCode(model.shortCode))
  }
}
