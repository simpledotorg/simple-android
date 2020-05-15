package org.simple.clinic.deeplink

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.user.User
import java.util.UUID

class DeepLinkUpdateTest {

  private val updateSpec = UpdateSpec(DeepLinkUpdate())
  private val patientUuid = UUID.fromString("f88cc05b-620a-490a-92f3-1c0c43fb76ab")

  @Test
  fun `if there is no user logged in, then open setup activity`() {
    val defaultModel = DeepLinkModel.default()

    updateSpec
        .given(defaultModel)
        .whenEvent(UserFetched(null))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(NavigateToSetupActivity as DeepLinkEffect)
        ))
  }

  @Test
  fun `if there is a logged in user and patient uuid is null, then navigate to main activity`() {
    val model = DeepLinkModel.default()
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("dc0a9d11-aee4-4792-820f-c5cb66ae5e47"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN
    )

    updateSpec
        .given(model)
        .whenEvent(UserFetched(user))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(NavigateToMainActivity as DeepLinkEffect)
        ))
  }
}
