package org.simple.clinic.enterotp

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.After
import org.junit.Test
import org.simple.clinic.login.LoginResult
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

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
}
