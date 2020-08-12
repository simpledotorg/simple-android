package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import java.util.UUID

class AppLockScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<AppLockScreen>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val lastUnlockTimestamp = mock<Preference<Instant>>()

  private val loggedInUser = TestData.loggedInUser(
      uuid = UUID.fromString("cdb08a78-7bae-44f4-9bb9-40257be58aa4"),
      pinDigest = "actual-hash"
  )

  private val uiEvents = PublishSubject.create<UiEvent>()

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when PIN is authenticated, the last-unlock-timestamp should be updated and then the app should be unlocked`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("f0e27682-796c-47c9-8187-dbcca66c4273"),
        name = "PHC Obvious"
    )

    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.just(facility))

    // when
    setupController()
    uiEvents.onNext(AppLockPinAuthenticated())

    // then
    verify(lastUnlockTimestamp).delete()

    verify(screen).setUserFullName(loggedInUser.fullName)
    verify(screen).setFacilityName(facility.name)
    verify(screen).restorePreviousScreen()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `On start, the logged in user's full name should be shown`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("6dcb2c31-569e-4911-a378-046faa5fa9ff"),
        name = "PHC Obvious"
    )

    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.just(facility))

    // when
    setupController()

    // then
    verify(screen).setUserFullName(loggedInUser.fullName)
    verify(screen).setFacilityName(facility.name)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `On start, the currently selected facility should be shown`() {
    // given
    val facility1 = TestData.facility(name = "facility1")
    val facility2 = TestData.facility(name = "facility2")

    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.just(facility1, facility2))

    // when
    setupController()

    // then
    verify(screen).setUserFullName(loggedInUser.fullName)
    verify(screen).setFacilityName(facility1.name)
    verify(screen).setFacilityName(facility2.name)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when forgot pin is clicked then the confirm forgot pin alert must be shown`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("6dcb2c31-569e-4911-a378-046faa5fa9ff"),
        name = "PHC Obvious"
    )

    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.just(facility))

    // when
    setupController()
    uiEvents.onNext(AppLockForgotPinClicked())

    // then
    verify(screen).setUserFullName(loggedInUser.fullName)
    verify(screen).setFacilityName(facility.name)
    verify(screen).showConfirmResetPinDialog()
    verifyNoMoreInteractions(screen)
  }

  private fun setupController() {
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(loggedInUser))

    val controller = AppLockScreenController(userSession, facilityRepository, lastUnlockTimestamp)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(AppLockScreenCreated())
  }
}
