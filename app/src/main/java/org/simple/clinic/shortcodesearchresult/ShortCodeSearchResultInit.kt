package org.simple.clinic.shortcodesearchresult

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class ShortCodeSearchResultInit : Init<ShortCodeSearchResultState, ShortCodeSearchResultEffect> {

  override fun init(model: ShortCodeSearchResultState): First<ShortCodeSearchResultState, ShortCodeSearchResultEffect> {
    return first(model)
  }
}
