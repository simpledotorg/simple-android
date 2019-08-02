package org.simple.clinic.forgotpin.confirmpin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.ForgotPinResult.NetworkError
import org.simple.clinic.user.ForgotPinResult.Success
import org.simple.clinic.user.ForgotPinResult.UnexpectedError
import org.simple.clinic.user.ForgotPinResult.UserNotFound
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class ForgotPinConfirmPinScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val screen = mock<ForgotPinConfirmPinScreen>()

  private val loggedInUser = PatientMocker.loggedInUser(uuid = UUID.fromString("324d7648-e2a5-4192-831f-533b81181dc2"))
  private val facility = PatientMocker.facility()

  private val controller = ForgotPinConfirmPinScreenController(
      userSession = userSession,
      facilityRepository = facilityRepository,
      patientRepository = patientRepository
  )

  // FIXME 02-08-2019 : Fix tests with unexpected errors are passing even when stubs are not passed
  // We use onErrorReturn in one of the chains to wrap unexpected errors. However, when we forget
  // to stub certain calls in Mockito (UserSession#updateLoggedInStatusForUser), the tests still
  // pass (even though they shouldn't) because we are using onErrorReturn to wrap errors from
  // upstream. This needs to be fixed so that tests behave properly.

  @Before
  fun setUp() {
    whenever(userSession.requireLoggedInUser())
        .thenReturn(Observable.just(loggedInUser))
    whenever(facilityRepository.currentFacility(any<User>()))
        .thenReturn(Observable.just(facility))

    uiEvents
        .compose(controller)
        .subscribe { it.invoke(screen) }
  }

  @Test
  fun `on start, the logged in user's full name must be shown`() {
    // when
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("1111"))

    // then
    verify(screen).showUserName(loggedInUser.fullName)
  }

  @Test
  fun `on start, the current selected facility should be shown`() {
    // when
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("1111"))

    // then
    verify(screen).showFacility(facility.name)
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
    // given
    whenever(userSession.syncAndClearData(any(), any(), any()))
        .thenReturn(Completable.complete())
    whenever(userSession.resetPin(originalPin))
        .thenReturn(Single.just(Success))

    // when
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated(originalPin))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(submittedPin1))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(submittedPin2))

    // then
    if (bothAreFailures) {
      verify(screen, times(2)).showPinMismatchedError()
    } else {
      verify(screen).showPinMismatchedError()
    }
  }

  @Test
  fun `when PIN is changed, any errors must be hidden`() {
    // when
    uiEvents.onNext(ForgotPinConfirmPinTextChanged("1"))
    uiEvents.onNext(ForgotPinConfirmPinTextChanged("11"))
    uiEvents.onNext(ForgotPinConfirmPinTextChanged("111"))

    // then
    verify(screen, times(3)).hideError()
  }

  @Test
  fun `when a valid PIN is submitted, the local patient data must be synced and then cleared`() {
    // given
    val pin = "0000"
    whenever(userSession.syncAndClearData(any(), any(), any()))
        .thenReturn(Completable.complete())

    // when
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated(pin))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(userSession).syncAndClearData(any(), any(), any())
  }

  @Test
  fun `when the sync fails, it must show an unexpected error`() {
    // given
    whenever(userSession.syncAndClearData(any(), any(), any()))
        .thenReturn(Completable.error(RuntimeException()))

    // when
    val pin = "0000"
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated(pin))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(screen).showUnexpectedError()
  }

  @Test
  @Parameters(value = [
    "0|false",
    "00|false",
    "000|false",
    "0000|true",
    "00000|false"
  ])
  fun `when a valid PIN is submitted and sync succeeds, it must raise the Reset PIN request`(
      pin: String,
      shouldRaiseRequest: Boolean
  ) {
    // given
    whenever(userSession.resetPin(pin))
        .thenReturn(Single.just(Success))
    whenever(userSession.syncAndClearData(any(), any(), any()))
        .thenReturn(Completable.complete())
    whenever(userSession.updateLoggedInStatusForUser(loggedInUser.uuid, User.LoggedInStatus.RESETTING_PIN))
        .thenReturn(Completable.complete())

    // when
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("0000"))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    if (shouldRaiseRequest) {
      verify(userSession).resetPin(pin)
    } else {
      verify(userSession, never()).resetPin(any())
    }
  }

  @Test
  fun `when a valid PIN is submitted, the progress must be shown`() {
    // when
    val pin = "0000"
    whenever(userSession.syncAndClearData(any(), any(), any()))
        .thenReturn(Completable.complete())
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated(pin))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(screen).showProgress()
  }

  @Test
  fun `when the forgot PIN call fails with a network error, the error must be shown`() {
    // given
    val pin = "0000"
    whenever(userSession.syncAndClearData(any(), any(), any()))
        .thenReturn(Completable.complete())
    whenever(userSession.updateLoggedInStatusForUser(loggedInUser.uuid, User.LoggedInStatus.RESETTING_PIN))
        .thenReturn(Completable.complete())
    whenever(userSession.resetPin(pin))
        .thenReturn(Single.just(NetworkError))

    // when
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated(pin))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(screen).showNetworkError()
  }

  @Test
  fun `when the forgot PIN call fails with an unexpected error, the error must be shown`() {
    // given
    val pin = "0000"
    whenever(userSession.syncAndClearData(any(), any(), any()))
        .thenReturn(Completable.complete())
    whenever(userSession.resetPin(pin))
        .thenReturn(Single.just(UnexpectedError(RuntimeException())))

    // when
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated(pin))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when the forgot PIN call fails with a user not found error, the error must be shown`() {
    // given
    val pin = "0000"
    whenever(userSession.syncAndClearData(any(), any(), any()))
        .thenReturn(Completable.complete())
    whenever(userSession.resetPin(pin))
        .thenReturn(Single.just(UserNotFound))

    // when
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated(pin))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when the forgot PIN call succeeds, the home screen must be opened`() {
    // given
    val pin = "0000"
    whenever(userSession.syncAndClearData(any(), any(), any()))
        .thenReturn(Completable.complete())
    whenever(userSession.updateLoggedInStatusForUser(loggedInUser.uuid, User.LoggedInStatus.RESETTING_PIN))
        .thenReturn(Completable.complete())
    whenever(userSession.resetPin(pin))
        .thenReturn(Single.just(Success))

    // then
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated(pin))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(screen).goToHomeScreen()
  }

  @Test
  fun `when resetting the PIN and data gets cleared, the user logged in status must be set to RESETTING_PIN`() {
    // given
    whenever(userSession.syncAndClearData(any(), any(), any()))
        .thenReturn(Completable.complete())

    // when
    uiEvents.onNext(ForgotPinConfirmPinScreenCreated("0000"))
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("0000"))

    // then
    verify(userSession).updateLoggedInStatusForUser(loggedInUser.uuid, User.LoggedInStatus.RESETTING_PIN)
  }
}
