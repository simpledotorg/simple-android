package org.simple.clinic.shortcodesearchresult

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class ShortCodeSearchResultUpdate: Update<ShortCodeSearchResultState, ShortCodeSearchResultEvent, ShortCodeSearchResultEffect> {

  override fun update(
      model: ShortCodeSearchResultState,
      event: ShortCodeSearchResultEvent
  ): Next<ShortCodeSearchResultState, ShortCodeSearchResultEffect> {
    return noChange()
  }
}
