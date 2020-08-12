package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
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
  lateinit var controller: AppLockScreenController

  @Before
  fun setUp() {
    controller = AppLockScreenController(userSession, facilityRepository, lastUnlockTimestamp)
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(loggedInUser))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when PIN is authenticated, the last-unlock-timestamp should be updated and then the app should be unlocked`() {
    uiEvents.onNext(AppLockPinAuthenticated())

    val inOrder = inOrder(lastUnlockTimestamp, screen)
    inOrder.verify(lastUnlockTimestamp).delete()
    inOrder.verify(screen).restorePreviousScreen()
  }

  @Test
  fun `On start, the logged in user's full name should be shown`() {
    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.never())

    uiEvents.onNext(AppLockScreenCreated())
    verify(screen).setUserFullName(loggedInUser.fullName)
  }

  @Test
  fun `On start, the currently selected facility should be shown`() {
    val facility1 = TestData.facility(name = "facility1")
    val facility2 = TestData.facility(name = "facility2")
    whenever(facilityRepository.currentFacility(loggedInUser)).thenReturn(Observable.just(facility1, facility2))

    uiEvents.onNext(AppLockScreenCreated())
    verify(screen).setFacilityName(facility1.name)
    verify(screen).setFacilityName(facility2.name)
  }

  @Test
  fun `when forgot pin is clicked then the confirm forgot pin alert must be shown`() {
    uiEvents.onNext(AppLockForgotPinClicked())
    verify(screen).showConfirmResetPinDialog()
  }
}
