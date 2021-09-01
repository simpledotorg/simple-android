package org.simple.clinic.enterotp

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState.Allowed
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState.Blocked
import org.simple.clinic.login.LoginResult.NetworkError
import org.simple.clinic.login.LoginResult.ServerError
import org.simple.clinic.login.LoginResult.UnexpectedError
import java.time.Instant
import java.util.UUID

class EnterOtpUpdateTest {
  private val updateSpec = UpdateSpec(EnterOtpUpdate(loginOtpRequiredLength = 6))
  private val user = TestData.loggedInUser(uuid = UUID.fromString("6fc16e72-39a5-4568-86db-1f8b1c0c08d3"))
  private val loginStartedModel = EnterOtpModel.create()
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
                hasNoModel(),
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
                hasNoModel(),
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
                hasNoModel(),
                hasEffects(ShowUnexpectedError, ClearPin)
            )
        )
  }

  @Test
  fun `when otp entry protected state is changed and is allowed, then allow otp entry`() {
    updateSpec
        .given(loginStartedModel)
        .whenEvent(OtpEntryProtectedStateChanged(Allowed(2, 3)))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(AllowOtpEntry)
            )
        )
  }

  @Test
  fun `when otp protected state is allowed and attempts are 0, then hide the errors`() {
    updateSpec
        .given(loginStartedModel)
        .whenEvent(OtpEntryProtectedStateChanged(stateChanged = Allowed(0, attemptsRemaining = 5)))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(HideErrors)
            )
        )
  }

  @Test
  fun `when otp protected state is blocked, then block otp entry until 20 minutes and show otp limit reached error`() {
    val blockedUntil = Instant.parse("2021-09-01T00:00:00Z")
    val attemptsMade = 5
    updateSpec
        .given(loginStartedModel)
        .whenEvent(OtpEntryProtectedStateChanged(stateChanged = Blocked(attemptsMade = attemptsMade, blockedTill = blockedUntil)))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowIncorrectOtpLimitReachedError(attemptsMade), BlockOtpEntryUntil(blockedUntil))
            )
        )
  }

  @Test
  fun `when user has made attempts, then show incorrect otp error attempts message`() {
    val attemptsMade = 1
    val attemptsRemaining = 4
    updateSpec
        .given(loginStartedModel)
        .whenEvent(OtpEntryProtectedStateChanged(stateChanged = Allowed(attemptsMade, attemptsRemaining)))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowIncorrectOtpError(attemptsMade, attemptsRemaining))
            )
        )
  }
}
