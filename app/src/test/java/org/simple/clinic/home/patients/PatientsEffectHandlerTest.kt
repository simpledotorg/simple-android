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
import org.simple.clinic.drugstockreminders.DrugStockReminder
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.NotFound
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.refreshuser.RefreshCurrentUser
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.util.toUtcInstant
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.TestUserClock
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Instant
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

class PatientsEffectHandlerTest {
  private val uiActions = mock<PatientsTabUiActions>()
  private val userSession = mock<UserSession>()
  private val approvalStatusApprovedAtPreference = mock<Preference<Instant>>()
  private val hasUserDismissedApprovedStatusPreference = mock<Preference<Boolean>>()
  private val checkAppUpdate = mock<CheckAppUpdateAvailability>()
  private val appUpdateDialogShownPref = mock<Preference<Instant>>()
  private val appUpdateNotificationScheduler = mock<AppUpdateNotificationScheduler>()
  private val drugStockReminder = mock<DrugStockReminder>()
  private val drugStockReportLastCheckedAt = mock<Preference<Instant>>()
  private val isDrugStockReportFilled = mock<Preference<Optional<Boolean>>>()

  private val date = LocalDate.parse("2018-01-01")
  private val utcClock = TestUtcClock(date)
  private val userClock = TestUserClock(date)

  private val numberOfPatientsRegisteredPreference = mock<Preference<Int>>()
  private val refreshCurrentUser = mock<RefreshCurrentUser>()

  private val viewEffectHandler = PatientsTabViewEffectHandler(uiActions)

  private val facility = TestData.facility(
      uuid = UUID.fromString("7e599a46-c9f4-40f6-8f9f-16b650640157"),
      name = "PHC Obvious"
  )
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
      approvalStatusUpdatedAtPref = approvalStatusApprovedAtPreference,
      drugStockReminder = drugStockReminder,
      drugStockReportLastCheckedAt = drugStockReportLastCheckedAt,
      isDrugStockReportFilled = isDrugStockReportFilled,
      currentFacility = Observable.just(facility),
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
    whenever(checkAppUpdate.listen()).doReturn(Observable.just(ShowAppUpdate(appUpdateNudgePriority, 181)))
    whenever(appUpdateDialogShownPref.get()).thenReturn(appUpdateLastShownOn.toUtcInstant(userClock))

    // when
    effectHandlerTestCase.dispatch(LoadInfoForShowingAppUpdateMessage)

    // then

    effectHandlerTestCase.assertOutgoingEvents(RequiredInfoForShowingAppUpdateLoaded(
        isAppUpdateAvailable = true,
        appUpdateLastShownOn = appUpdateLastShownOn,
        currentDate = LocalDate.of(2018, 1, 1),
        appUpdateNudgePriority = appUpdateNudgePriority,
        appStaleness = 181
    ))
  }

  @Test
  fun `when load drug stock reminder status effect is received, then load the drug stock reminder status`() {
    // given
    whenever(drugStockReminder.reminderForDrugStock("2022-03-02")) doReturn NotFound

    // when
    effectHandlerTestCase.dispatch(LoadDrugStockReportStatus(date = "2022-03-02"))

    // then
    effectHandlerTestCase.assertOutgoingEvents(DrugStockReportLoaded(result = NotFound))

    verifyZeroInteractions(uiActions)

    verify(drugStockReminder).reminderForDrugStock(date = "2022-03-02")
    verifyNoMoreInteractions(drugStockReminder)
  }

  @Test
  fun `when load info for showing drug stock reminder effect is received, then load the info`() {
    // given
    whenever(drugStockReportLastCheckedAt.get()) doReturn LocalDate.parse("2022-04-19").toUtcInstant(userClock)
    whenever(isDrugStockReportFilled.get()) doReturn Optional.of(true)

    // when
    effectHandlerTestCase.dispatch(LoadInfoForShowingDrugStockReminder)

    // then
    effectHandlerTestCase.assertOutgoingEvents(RequiredInfoForShowingDrugStockReminderLoaded(
        currentDate = LocalDate.parse("2018-01-01"),
        drugStockReportLastCheckedAt = LocalDate.parse("2022-04-19"),
        isDrugStockReportFilled = Optional.of(true)
    ))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when touch drug stock report last checked at preference effect is received, then update drug stock report last checked at preference`() {
    // when
    effectHandlerTestCase.dispatch(TouchDrugStockReportLastCheckedAt)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verifyZeroInteractions(uiActions)

    verify(drugStockReportLastCheckedAt).set(Instant.parse("2018-01-01T00:00:00Z"))
    verifyNoMoreInteractions(drugStockReportLastCheckedAt)
  }

  @Test
  fun `when touch is drug stock report filled effect is received, then update drug stock report filled status in preference`() {
    // when
    effectHandlerTestCase.dispatch(TouchIsDrugStockReportFilled(isDrugStockReportFilled = true))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verifyZeroInteractions(uiActions)

    verify(isDrugStockReportFilled).set(Optional.of(true))
    verifyNoMoreInteractions(drugStockReportLastCheckedAt)
  }

  @Test
  fun `when open enter drug stock screen effect is received, then open enter drug stock screen`() {
    // when
    effectHandlerTestCase.dispatch(OpenEnterDrugStockScreen)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).openEnterDrugStockScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show no active network connection dialog effect is received, then show no active network connection dialog`() {
    // when
    effectHandlerTestCase.dispatch(ShowNoActiveNetworkConnectionDialog)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).showNoActiveNetworkConnectionDialog()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load current facility effect is received, then load current facility`() {
    // when
    effectHandlerTestCase.dispatch(LoadCurrentFacility)

    // then
    effectHandlerTestCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyZeroInteractions(uiActions)
  }
}
