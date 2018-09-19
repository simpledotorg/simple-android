package org.simple.clinic.forgotpin.confirmpin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

class ForgotPinConfirmPinScreenControllerTest {

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()

  private lateinit var controller: ForgotPinConfirmPinScreenController
  private lateinit var screen: ForgotPinConfirmPinScreen

  private val loggedInUser = PatientMocker.loggedInUser()
  private val facility = PatientMocker.facility()

  @Before
  fun setUp() {
    screen = mock()

    controller = ForgotPinConfirmPinScreenController(userSession, facilityRepository)
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(loggedInUser))
    whenever(facilityRepository.currentFacility(any<User>())).thenReturn(Observable.just(facility))

    uiEvents.compose(controller)
        .subscribe { it.invoke(screen) }
  }

  @Test
  fun `on start, the logged in user's full name must be shown`() {
    uiEvents.onNext(ScreenCreated())

    verify(screen).showUserName(loggedInUser.fullName)
  }

  @Test
  fun `on start, the current selected facility should be shown`() {
    uiEvents.onNext(ScreenCreated())

    verify(screen).showFacility(facility.name)
  }

  @Test
  fun `when the facility name is clicked then facility change screen should be shown`() {
    uiEvents.onNext(ForgotPinConfirmPinScreenFacilityClicked)

    verify(screen).openFacilityChangeScreen()
  }

  @Test
  fun `when pressing the back button the screen must be closed`() {
    uiEvents.onNext(ForgotPinConfirmPinScreenBackClicked)

    verify(screen).goBack()
  }
}
