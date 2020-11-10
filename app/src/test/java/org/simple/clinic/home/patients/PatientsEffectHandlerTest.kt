package org.simple.clinic.home.patients

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class PatientsEffectHandlerTest {

  private val uiActions = mock<PatientsTabUiActions>()

  private val testCase = EffectHandlerTestCase(PatientsEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      refreshCurrentUser = mock(),
      userSession = mock(),
      utcClock = TestUtcClock(),
      userClock = TestUserClock(),
      checkAppUpdate = mock(),
      approvalStatusUpdatedAtPref = mock(),
      hasUserDismissedApprovedStatusPref = mock(),
      numberOfPatientsRegisteredPref = mock(),
      appUpdateDialogShownAtPref = mock(),
      uiActions = uiActions
  ).build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the open short code search screen effect is received, the short code search screen must be opened`() {
    // when
    val shortCode = "1234567"
    testCase.dispatch(OpenShortCodeSearchScreen(shortCode))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openShortCodeSearchScreen(shortCode)
    verifyNoMoreInteractions(uiActions)
  }
}
