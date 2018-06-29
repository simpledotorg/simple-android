package org.simple.clinic.login.pin

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.login.LoginResult
import org.simple.clinic.sync.SyncScheduler
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class LoginPinScreenControllerTest {

  private val screen = mock<LoginPinScreen>()
  private val userSession = mock<UserSession>()
  private val syncScheduler = mock<SyncScheduler>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  lateinit var controller: LoginPinScreenController

  @Before
  fun setUp() {
    controller = LoginPinScreenController(userSession, syncScheduler)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }

    whenever(syncScheduler.syncImmediately()).thenReturn(Completable.complete())
  }

  @Test
  fun `when screen starts, show phone number`() {
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(OngoingLoginEntry(otp = "3444", phoneNumber = "123")))

    uiEvents.onNext(PinScreenCreated())

    verify(userSession).ongoingLoginEntry()
    verify(screen).showPhoneNumber("123")
  }

  @Test
  fun `if pin is not empty, and submit is clicked, make login api call, and open home screen`() {
    val ongoingEntry = OngoingLoginEntry(otp = "123", phoneNumber = "99999")
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.login()).thenReturn(Single.just(LoginResult.Success()))

    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(PinSubmitClicked())

    val inOrder = inOrder(userSession, screen)
    inOrder.verify(userSession).login()
    inOrder.verify(userSession).ongoingLoginEntry()
    inOrder.verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(otp = "123", phoneNumber = "99999", pin = "0000"))
    inOrder.verify(screen).showProgressBar()
    inOrder.verify(screen).hideProgressBar()
    inOrder.verify(screen).openHomeScreen()
  }

  @Test
  fun `if login api call throws any errors, show errors`() {
    val ongoingEntry = OngoingLoginEntry(otp = "123", phoneNumber = "99999")
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.login())
        .thenReturn(Single.just(LoginResult.NetworkError()))
        .thenReturn(Single.just(LoginResult.ServerError("Server error")))
        .thenReturn(Single.just(LoginResult.UnexpectedError()))

    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(PinSubmitClicked())
    uiEvents.onNext(PinSubmitClicked())
    uiEvents.onNext(PinSubmitClicked())

    verify(screen).showNetworkError()
    verify(screen).showServerError("Server error")
    verify(screen).showUnexpectedError()
  }

  @Test
  @Parameters(method = "values for data sync")
  fun `data should only be synced when login succeeds`(loginResult: LoginResult, shouldSync: Boolean) {
    whenever(syncScheduler.syncImmediately()).thenReturn(Completable.complete())

    val ongoingEntry = OngoingLoginEntry(otp = "123", phoneNumber = "99999")
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())
    whenever(userSession.login()).thenReturn(Single.just(loginResult))

    uiEvents.onNext(PinTextChanged("0000"))
    uiEvents.onNext(PinSubmitClicked())

    if (shouldSync) {
      verify(syncScheduler).syncImmediately()
    } else {
      verify(syncScheduler, never()).syncImmediately()
    }
  }

  fun `values for data sync`(): Array<Any> {
    return arrayOf(
        arrayOf(LoginResult.Success(), true),
        arrayOf(LoginResult.ServerError("some error"), false),
        arrayOf(LoginResult.UnexpectedError(), false),
        arrayOf(LoginResult.NetworkError(), false)
    )
  }
}
