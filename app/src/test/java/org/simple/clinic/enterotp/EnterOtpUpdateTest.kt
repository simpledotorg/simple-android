package org.simple.clinic.enterotp

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.login.LoginResult.NetworkError
import org.simple.clinic.login.LoginResult.ServerError
import java.util.UUID

class EnterOtpUpdateTest {
  private val updateSpec = UpdateSpec(EnterOtpUpdate(loginOtpRequiredLength = 6))
  private val user = TestData.loggedInUser(uuid = UUID.fromString("6fc16e72-39a5-4568-86db-1f8b1c0c08d3"))
  private val loginStartedModel = EnterOtpModel.create()
      .userLoaded(user)
      .enteredOtpValid()
      .loginStarted()

  @Test
  fun `when the login request is completed and is unsuccessful, then finish logging in and show error`() {
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

  @Test
  fun `when the login request is completed and has returned server error saying incorrect otp, then show failed attempt and clear pin`() {
    val incorrectOtp = "Your entered Otp is incorrect, please try again"
    val result = ServerError(incorrectOtp)
    updateSpec
        .given(loginStartedModel)
        .whenEvent(LoginUserCompleted(result))
        .then(
            assertThatNext(
                hasModel(loginStartedModel.loginFinished().loginFailed(AsyncOpError.Companion.from(result))),
                hasEffects(FailedLoginOtpAttempt(result), ClearPin)
            )
        )
  }
}
