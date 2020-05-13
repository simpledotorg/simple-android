package org.simple.clinic.deeplink

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class DeepLinkUpdateTest {

  @Test
  fun `if there is no user logged in, then open setup activity`() {
    val updateSpec = UpdateSpec(DeepLinkUpdate())
    val defaultModel = DeepLinkModel.default()

    updateSpec
        .given(defaultModel)
        .whenEvent(UserFetched(null))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(NavigateToSetupActivity as DeepLinkEffect)
        ))
  }
}
