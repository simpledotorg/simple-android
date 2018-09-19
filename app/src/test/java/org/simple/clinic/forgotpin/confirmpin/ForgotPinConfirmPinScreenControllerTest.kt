package org.simple.clinic.forgotpin.confirmpin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
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
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("1111"))

    verify(screen).showUserName(loggedInUser.fullName)
  }

  @Test
  fun `on start, the current selected facility should be shown`() {
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("1111"))

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

  @Test
  @Parameters(value = [
    "0000|000|0000|false",
    "0000|000|001|true",
    "0000|0000|000|false"
  ])
  fun `when submitting a PIN that does not match the previous PIN, an error should be shown`(
      originalPin: String,
      submittedPin1: String,
      submittedPin2: String,
      bothAreFailures: Boolean
  ) {
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated(originalPin))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(submittedPin1))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(submittedPin2))

    if (bothAreFailures) {
      verify(screen, times(2)).showPinMismatchedError()
    } else {
      verify(screen).showPinMismatchedError()
    }
  }

  @Test
  fun `when PIN is changed, any errors must be hidden`() {
    uiEvents.onNext(ForgotPinConfirmPinTextChanged("1"))
    uiEvents.onNext(ForgotPinConfirmPinTextChanged("11"))
    uiEvents.onNext(ForgotPinConfirmPinTextChanged("111"))

    verify(screen, times(3)).hideError()
  }
}
