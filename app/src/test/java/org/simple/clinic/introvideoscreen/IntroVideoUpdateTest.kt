package org.simple.clinic.introvideoscreen

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class IntroVideoUpdateTest {

  @Test
  fun `when video is clicked, then open the video and home screen`() {
    val updateSpec = UpdateSpec(IntroVideoUpdate())
    val defaultModel = IntroVideoModel.default()

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
}
