package org.simple.clinic.introvideoscreen

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class IntroVideoUpdate : Update<IntroVideoModel, IntroVideoEvent, IntroVideoEffect> {
  override fun update(model: IntroVideoModel, event: IntroVideoEvent): Next<IntroVideoModel,
      IntroVideoEffect> {
    return when (event) {
      VideoClicked -> dispatch(OpenVideo, OpenHome)
      SkipClicked -> dispatch(OpenHome)
    }
  }
}
