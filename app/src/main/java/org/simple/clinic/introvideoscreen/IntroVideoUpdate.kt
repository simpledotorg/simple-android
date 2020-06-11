package org.simple.clinic.introvideoscreen

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class IntroVideoUpdate : Update<IntroVideoModel, IntroVideoEvent, IntroVideoEffect> {
  override fun update(model: IntroVideoModel, event: IntroVideoEvent): Next<IntroVideoModel,
      IntroVideoEffect> = noChange()
}
