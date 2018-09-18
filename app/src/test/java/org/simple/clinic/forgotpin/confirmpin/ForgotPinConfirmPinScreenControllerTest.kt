package org.simple.clinic.forgotpin.confirmpin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.ForgotPinResult
import org.simple.clinic.user.ForgotPinResult.NetworkError
import org.simple.clinic.user.ForgotPinResult.Success
import org.simple.clinic.user.ForgotPinResult.UnexpectedError
import org.simple.clinic.user.ForgotPinResult.UserNotFound
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
    whenever(userSession.resetPin(any())).thenReturn(Single.just(Success))

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

  @Test
  @Parameters(value = [
    "0|false",
    "00|false",
    "000|false",
    "0000|true",
    "00000|false"
  ])
  fun `when a valid PIN is submitted, it must raise the Reset PIN request`(
      pin: String,
      shouldRaiseRequest: Boolean
  ) {
    whenever(userSession.resetPin(any())).thenReturn(Single.just(Success))

    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("0000"))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    if (shouldRaiseRequest) {
      verify(userSession).resetPin(pin)
    } else {
      verify(userSession, never()).resetPin(any())
    }
  }

  @Test
  fun `when a valid PIN is submitted and reset PIN call fails, it must show the unexpected error`() {
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("0000"))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("0000"))

    verify(screen).showUnexpectedError()
    verify(userSession, never()).resetPin(any())
  }

  @Test
  fun `when a valid PIN is submitted, the progress must be shown`() {
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("0000"))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("0000"))

    verify(screen).showProgress()
  }

  @Test
  @Parameters(method = "params For failed forgot pin call")
  fun `when the forgot PIN call completes, the progress must be hidden`(result: Single<ForgotPinResult>) {
    whenever(userSession.resetPin(any())).thenReturn(result)

    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("0000"))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("0000"))

    verify(screen).hideProgress()
  }

  // Accessed via reflection
  @Suppress("Unused")
  private fun `params For failed forgot pin call`(): Array<Any> {
    return arrayOf(
        Single.just(Success),
        Single.just(NetworkError),
        Single.just(UnexpectedError(RuntimeException())),
        Single.just(UserNotFound)
    )
  }

  @Test
  fun `when the forgot PIN call fails with a network error, the error must be shown`() {
    whenever(userSession.resetPin(any())).thenReturn(Single.just(NetworkError))

    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("0000"))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("0000"))

    verify(screen).showNetworkError()
  }

  @Test
  fun `when the forgot PIN call fails with an unexpected error, the error must be shown`() {
    whenever(userSession.resetPin(any())).thenReturn(Single.just(UnexpectedError(RuntimeException())))

    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("0000"))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("0000"))

    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when the forgot PIN call fails with a user not found error, the error must be shown`() {
    whenever(userSession.resetPin(any())).thenReturn(Single.just(UserNotFound))

    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("0000"))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("0000"))

    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when the forgot PIN call succeeds, the home screen must be opened`() {
    whenever(userSession.resetPin(any())).thenReturn(Single.just(Success))

    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("0000"))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("0000"))

    verify(screen).goToHomeScreen()
  }
}
