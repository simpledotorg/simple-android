package org.simple.clinic.enterotp

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState.Allowed
import org.simple.clinic.enterotp.BruteForceOtpEntryProtection.ProtectedState.Blocked
import org.simple.clinic.enterotp.OtpEntryMode.BruteForceOtpEntryLocked
import org.simple.clinic.enterotp.OtpEntryMode.OtpEntry
import java.time.Instant

class EnterOtpUiRendererTest {
  private val ui = mock<EnterOtpUi>()
  private val uiRenderer = EnterOtpUiRenderer(ui)
  private val model = EnterOtpModel.create()

  @Test
  fun `when the protected state is allowed, then allow otp entries`() {
    // given
    val attemptsMade = 0
    val attemptsRemaining = 5
    val allowed = Allowed(attemptsMade = attemptsMade,attemptsRemaining = attemptsRemaining)
    val updatedModel = model.setOtpEntryMode(allowed)

    // when
    uiRenderer.render(updatedModel)

    // then
    verify(ui).showOtpEntryMode(OtpEntry)
    verify(ui).hideError()
    verify(ui).hideProgress()
  }

  @Test
  fun `when the protected state is allowed, then allow otp entries with failed attempts error`() {
    // given
    val attemptsMade = 2
    val attemptsRemaining = 3
    val allowed = Allowed(attemptsMade = attemptsMade,attemptsRemaining = attemptsRemaining)
    val updatedModel = model.setOtpEntryMode(allowed)

    // when
    uiRenderer.render(updatedModel)

    // then
    verify(ui).showOtpEntryMode(OtpEntry)
    verify(ui).showFailedAttemptOtpError(attemptsMade, attemptsRemaining)
    verify(ui).hideProgress()
  }

  @Test
  fun `when the protected state is blocked, then block otp entries`() {
    // given
    val blockedUntil = Instant.parse("2021-09-02T00:00:00Z")
    val attemptsMade = 5
    val blocked = Blocked(attemptsMade = attemptsMade,blockedTill = blockedUntil)
    val updatedModel = model.setOtpEntryMode(blocked)

    // when
    uiRenderer.render(updatedModel)

    // then
    verify(ui).showOtpEntryMode(BruteForceOtpEntryLocked(blockedUntil))
    verify(ui).showLimitReachedError(attemptsMade)
  }
}
