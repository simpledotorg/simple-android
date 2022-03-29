package org.simple.clinic.home.patients

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.appupdate.AppUpdateNotificationScheduler
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL
import org.simple.clinic.appupdate.AppUpdateState.ShowAppUpdate
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.refreshuser.RefreshCurrentUser
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.util.toUtcInstant
import java.time.Instant
import java.time.LocalDate

class PatientsEffectHandlerTest {
  private val uiActions = mock<PatientsTabUiActions>()
  private val userSession = mock<UserSession>()
  private val approvalStatusApprovedAtPreference = mock<Preference<Instant>>()
  private val hasUserDismissedApprovedStatusPreference = mock<Preference<Boolean>>()
  private val checkAppUpdate = mock<CheckAppUpdateAvailability>()
  private val appUpdateDialogShownPref = mock<Preference<Instant>>()
  private val appUpdateNotificationScheduler = mock<AppUpdateNotificationScheduler>()

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
      appUpdateNotificationScheduler = appUpdateNotificationScheduler,
      hasUserDismissedApprovedStatusPref = hasUserDismissedApprovedStatusPreference,
      numberOfPatientsRegisteredPref = numberOfPatientsRegisteredPreference,
      appUpdateDialogShownAtPref = appUpdateDialogShownPref,
      viewEffectsConsumer = viewEffectHandler::handle,
      approvalStatusUpdatedAtPref = approvalStatusApprovedAtPreference
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

  @Test
  fun `when load app staleness effect is received, then load app staleness`() {
    // given
    val appStaleness = 35
    whenever(checkAppUpdate.loadAppStaleness()).doReturn(Observable.just(appStaleness))

    // when
    effectHandlerTestCase.dispatch(LoadAppStaleness)

    // then
    effectHandlerTestCase.assertOutgoingEvents(AppStalenessLoaded(appStaleness))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when schedule app update notification effect is received, then schedule app update notification`() {
    // when
    effectHandlerTestCase.dispatch(ScheduleAppUpdateNotification)

    // then
    verify(appUpdateNotificationScheduler).schedule()
    verifyNoMoreInteractions(appUpdateNotificationScheduler)
    effectHandlerTestCase.assertNoOutgoingEvents()
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when show critical app update dialog effect is received, then show critical app update dialog`() {
    // given
    val appUpdateNudgePriority = CRITICAL

    // when
    effectHandlerTestCase.dispatch(ShowCriticalAppUpdateDialog(appUpdateNudgePriority))

    // then
    verify(uiActions).showCriticalAppUpdateDialog(appUpdateNudgePriority)
    verifyNoMoreInteractions(uiActions)
    effectHandlerTestCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when load info for showing app update message effect is received, then load info for showing app update message`() {
    // given
    val appUpdateNudgePriority = CRITICAL
    val appUpdateLastShownOn = LocalDate.of(2022, 3, 22)
    whenever(checkAppUpdate.listen()).doReturn(Observable.just(ShowAppUpdate(appUpdateNudgePriority)))
    whenever(appUpdateDialogShownPref.get()).thenReturn(appUpdateLastShownOn.toUtcInstant(userClock))

    // when
    effectHandlerTestCase.dispatch(LoadInfoForShowingAppUpdateMessage)

    // then

    effectHandlerTestCase.assertOutgoingEvents(RequiredInfoForShowingAppUpdateLoaded(
        isAppUpdateAvailable = true,
        appUpdateLastShownOn = appUpdateLastShownOn,
        currentDate = LocalDate.of(2018, 1, 1),
        appUpdateNudgePriority = appUpdateNudgePriority
    ))
  }
}
