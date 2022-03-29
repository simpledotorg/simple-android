package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Test
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.refreshuser.RefreshCurrentUser
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Instant
import java.time.LocalDate

class PatientsEffectHandlerTest {
  private val uiActions = mock<PatientsTabUiActions>()
  private val userSession = mock<UserSession>()
  private val approvalStatusApprovedAtPreference = mock<Preference<Instant>>()
  private val hasUserDismissedApprovedStatusPreference = mock<Preference<Boolean>>()
  private val checkAppUpdate = mock<CheckAppUpdateAvailability>()
  private val appUpdateDialogShownPref = mock<Preference<Instant>>()

  private val date = LocalDate.parse("2018-01-01")
  private val utcClock = TestUtcClock(date)
  private val userClock = TestUserClock(date)

  private val numberOfPatientsRegisteredPreference = mock<Preference<Int>>()
  private val refreshCurrentUser = mock<RefreshCurrentUser>()

  private val viewEffectHandler = PatientsTabViewEffectHandler(uiActions)

  private val effectHandler = PatientsEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      refreshCurrentUser = refreshCurrentUser,
      userSession = userSession,
      utcClock = utcClock,
      userClock = userClock,
      checkAppUpdate = checkAppUpdate,
      approvalStatusUpdatedAtPref = approvalStatusApprovedAtPreference,
      hasUserDismissedApprovedStatusPref = hasUserDismissedApprovedStatusPreference,
      numberOfPatientsRegisteredPref = numberOfPatientsRegisteredPreference,
      appUpdateDialogShownAtPref = appUpdateDialogShownPref,
      viewEffectsConsumer = viewEffectHandler::handle
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when open simple on play store effect is received, then open simple on play store`() {
    // when
    effectHandlerTestCase.dispatch(OpenSimpleOnPlayStore)

    // then
    verify(uiActions).openSimpleOnPlaystore()
    effectHandlerTestCase.assertNoOutgoingEvents()
  }
}
