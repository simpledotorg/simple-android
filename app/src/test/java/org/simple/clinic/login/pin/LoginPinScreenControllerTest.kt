package org.simple.clinic.login.pin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.login.LoginResult
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

class LoginPinScreenControllerTest {

  private val screen = mock<LoginPinScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val controller = LoginPinScreenController()
  private val userSession = mock<UserSession>()

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `if pin is empty, disable the submit button, and vice versa`() {
    uiEvents.onNext(PinTextChanged(""))
    uiEvents.onNext(PinTextChanged("123"))
    uiEvents.onNext(PinTextChanged("1234"))

    verify(screen).enableSubmitButton(false)
    verify(screen, times(1)).enableSubmitButton(true)
  }

  @Test
  fun `if pin is not empty, and submit is clicked, make login api call, and open home screen`() {
    val ongoingEntry = OngoingLoginEntry(otp = "123", phoneNumber = "99999")
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.login()).thenReturn(Single.just(LoginResult.Success()))

    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(PinSubmitClicked())

    val inOrder = inOrder(userSession, screen)
    inOrder.verify(userSession).ongoingLoginEntry()
    inOrder.verify(screen).showProgressBar()
    inOrder.verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(otp = "123", phoneNumber = "99999", pin = "0000"))
    inOrder.verify(userSession).login()
    inOrder.verify(screen).hideProgressBar()
    inOrder.verify(screen).openHomeScreen()
  }

  @Test
  fun `if login api call throws any errors, show errors`() {
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())

    whenever(userSession.login())
        .thenReturn(Single.just(LoginResult.NetworkError()))
        .thenReturn(Single.just(LoginResult.ServerError("Earth dead")))
        .thenReturn(Single.just(LoginResult.UnexpectedError()))

    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(PinSubmitClicked())

    verify(screen).showNetworkError()
    verify(screen).showServerError("Earth dead")
    verify(screen).showUnexpectedError()
  }

  @Test
  fun `when screen starts, show phone number`() {
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(OngoingLoginEntry(otp = "3444", phoneNumber = "123")))

    uiEvents.onNext(PinScreenCreated())

    verify(userSession).ongoingLoginEntry()
    verify(screen).showPhoneNumber("123")
  }
}
