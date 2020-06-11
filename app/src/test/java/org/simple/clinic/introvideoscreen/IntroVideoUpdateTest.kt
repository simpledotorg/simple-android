package org.simple.clinic.introvideoscreen

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class IntroVideoUpdateTest {
  private val updateSpec = UpdateSpec(IntroVideoUpdate())
  private val defaultModel = IntroVideoModel.default()

  @Test
  fun `when video is clicked, then open the video and home screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(VideoClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenVideo, OpenHome)
            )
        )
  }

  @Test
  fun `when skip is clicked, then open the home screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(SkipClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenHome as IntroVideoEffect)
            )
        )
  }
}
