package org.simple.clinic.enterotp

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.login.LoginResult.NetworkError
import java.util.UUID

class EnterOtpUpdateTest {
  @Test
  fun `when the login request is completed and is unsuccessful, then finish logging in and show error`() {
    val updateSpec = UpdateSpec(EnterOtpUpdate(loginOtpRequiredLength = 6))
    val user = TestData.loggedInUser(uuid = UUID.fromString("6fc16e72-39a5-4568-86db-1f8b1c0c08d3"))
    val loginStartedModel = EnterOtpModel.create()
        .userLoaded(user)
        .enteredOtpValid()
        .loginStarted()

    updateSpec
        .given(loginStartedModel)
        .whenEvent(LoginUserCompleted(NetworkError))
        .then(
            assertThatNext(
                hasModel(loginStartedModel.loginFinished().loginFailed(AsyncOpError.Companion.from(NetworkError))),
                hasEffects(ClearPin)
            )
        )

  }
}