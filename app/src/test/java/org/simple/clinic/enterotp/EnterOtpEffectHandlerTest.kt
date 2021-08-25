package org.simple.clinic.enterotp

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.login.LoginResult
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Instant

class EnterOtpEffectHandlerTest {

  private val uiActions = mock<EnterOtpUiActions>()
  private val bruteForceOtpEntryProtection = mock<BruteForceOtpEntryProtection>()
  private val effectHandler = EnterOtpEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      userSession = mock(),
      dataSync = mock(),
      ongoingLoginEntryRepository = mock(),
      loginUserWithOtp = mock(),
      activateUser = mock(),
      bruteForceProtection = bruteForceOtpEntryProtection,
      uiActions = uiActions
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the failed login otp entry attempt effect is received, then increment the otp failed value in preferences`() {
    // given
    val errorMessage = "Your OTP does not match. Try again?"

    // when
    testCase.dispatch(FailedLoginOtpAttempt(LoginResult.ServerError(errorMessage)))

    // then
    verify(bruteForceOtpEntryProtection).incrementFailedOtpAttempt()
    verifyNoMoreInteractions(bruteForceOtpEntryProtection)
    verifyZeroInteractions(uiActions)
  }


  @Test
  fun `when show network error effect is received, then show network error`() {
    // when
    testCase.dispatch(ShowNetworkError)

    // then
    verify(uiActions).showNetworkError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show unexpected error effect is received, then show unexpected error`() {
    // when
    testCase.dispatch(ShowUnexpectedError)

    // then
    verify(uiActions).showUnexpectedError()
    verifyNoMoreInteractions(uiActions)
  }


  @Test
  fun `when allow otp entry effect is received, then allow otp entry`() {
    // when
    testCase.dispatch(AllowOtpEntry)
    
    // then
    verify(uiActions).showOtpEntryMode(OtpEntryMode.OtpEntry)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when block otp entry until effect is received, then show the block otp entry mode`() {
    // given
    val blockedUntil = Instant.parse("2021-08-27T00:00:00Z")
    // when
    testCase.dispatch(BlockOtpEntryUntil(blockedUntil))

    // then
    verify(uiActions).showOtpEntryMode(OtpEntryMode.BruteForceOtpEntryLocked(blockedUntil))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when hide errors effect is received, then hide the errors`() {
    // when
    testCase.dispatch(HideErrors)

    // then
    verify(uiActions).hideError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show incorrect Otp limit reached error effects has received, then show the error`() {
    // given
    val attemptsMade = 5

    // when
    testCase.dispatch(ShowIncorrectOtpLimitReachedError(attemptsMade))

    // then
    verify(uiActions).showLimitReachedError(attemptsMade)
    verifyNoMoreInteractions(uiActions)
  }
}
