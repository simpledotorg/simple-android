package org.simple.clinic.enterotp

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState.Allowed
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState.Blocked
import org.simple.clinic.login.LoginResult.NetworkError
import org.simple.clinic.login.LoginResult.ServerError
import org.simple.clinic.login.LoginResult.Success
import org.simple.clinic.login.LoginResult.UnexpectedError
import org.simple.clinic.login.activateuser.ActivateUser
import java.time.Instant
import java.util.UUID

class EnterOtpUpdateTest {
  private val updateSpec = UpdateSpec(EnterOtpUpdate(loginOtpRequiredLength = 6))
  private val user = TestData.loggedInUser(uuid = UUID.fromString("6fc16e72-39a5-4568-86db-1f8b1c0c08d3"))
  private val loginStartedModel = EnterOtpModel.create(minOtpRetries = 3, maxOtpEntriesAllowed = 5)
      .userLoaded(user)
      .enteredOtpValid()
      .loginStarted()

  @Test
  fun `when the login request is completed and has returned server error saying incorrect otp, then show failed attempt and clear pin`() {
    val incorrectOtp = "Your entered Otp is incorrect, please try again"
    val result = ServerError(incorrectOtp)
    updateSpec
        .given(loginStartedModel)
        .whenEvent(LoginUserCompleted(result))
        .then(
            assertThatNext(
                hasModel(loginStartedModel.loginFinished()),
                hasEffects(FailedLoginOtpAttempt(result), ClearPin)
            )
        )
  }

  @Test
  fun `when the login request is completed and has returned network error, then show show network error`() {
    val result = NetworkError
    updateSpec
        .given(loginStartedModel)
        .whenEvent(LoginUserCompleted(result))
        .then(
            assertThatNext(
                hasModel(loginStartedModel.loginFinished()),
                hasEffects(ShowNetworkError, ClearPin)
            )
        )
  }

  @Test
  fun `when the login request is completed and has returned unexpected error, then show show unexpected error`() {
    val result = UnexpectedError
    updateSpec
        .given(loginStartedModel)
        .whenEvent(LoginUserCompleted(result))
        .then(
            assertThatNext(
                hasModel(loginStartedModel.loginFinished()),
                hasEffects(ShowUnexpectedError, ClearPin)
            )
        )
  }

  @Test
  fun `when otp entry protected state is changed and is allowed, then allow otp entry`() {
    val allowed = Allowed(2, 3)
    updateSpec
        .given(loginStartedModel)
        .whenEvent(OtpEntryProtectedStateChanged(allowed))
        .then(
            assertThatNext(
                hasModel(loginStartedModel.setOtpEntryMode(allowed)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when otp protected state is blocked, then block otp entry until 20 minutes`() {
    val blockedUntil = Instant.parse("2021-09-01T00:00:00Z")
    val stateBlocked = Blocked(attemptsMade = 5, blockedTill = blockedUntil)
    updateSpec
        .given(loginStartedModel)
        .whenEvent(OtpEntryProtectedStateChanged(stateChanged = stateBlocked))
        .then(
            assertThatNext(
                hasModel(loginStartedModel.setOtpEntryMode(stateBlocked)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when the login request is completed and is successful, then clear pin and reset otp attempts`() {
    updateSpec
        .given(loginStartedModel)
        .whenEvent(LoginUserCompleted(Success))
        .then(
            assertThatNext(
                hasModel(loginStartedModel.loginFinished()),
                hasEffects(ClearLoginEntry, TriggerSync, ResetOtpAttemptLimit)
            )
        )
  }

  @Test
  fun `when request for otp has completed successfully, then request login otp and reset otp limit`() {
    val user = TestData.loggedInUserPayload(uuid = UUID.fromString("430081ec-8e36-478f-bd99-03abe95996b2"))
    updateSpec
        .given(loginStartedModel)
        .whenEvent(RequestLoginOtpCompleted(ActivateUser.Result.Success(userPayload = user)))
        .then(
            assertThatNext(
                hasModel(loginStartedModel.requestLoginOtpFinished()),
                hasEffects(ClearPin, ShowSmsSentMessage, ResetOtpAttemptLimit)
            )
        )
  }
}
