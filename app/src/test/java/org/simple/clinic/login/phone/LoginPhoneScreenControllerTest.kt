package org.simple.clinic.login.phone

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

class LoginPhoneScreenControllerTest {

  private val screen = mock<LoginPhoneScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val userSession = mock<UserSession>()

  lateinit var controller: LoginPhoneScreenController

  @Before
  fun setUp() {
    controller = LoginPhoneScreenController(userSession)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts, get OTP from intent, and create an OngoingLoginEntry`() {
    uiEvents.onNext(PhoneNumberScreenCreated("123"))

    verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry(otp = "123"))
  }

  @Test
  fun `if phone number is empty, disable the submit button, and vice versa`() {
    uiEvents.onNext(PhoneNumberTextChanged(""))
    uiEvents.onNext(PhoneNumberTextChanged("123"))
    uiEvents.onNext(PhoneNumberTextChanged("123444"))

    verify(screen).enableSubmitButton(false)
    verify(screen, times(1)).enableSubmitButton(true)
  }

  @Test
  fun `if phone number is not empty, and submit is clicked, create an OngoingLoginEntry, and go to pin screen`() {
    whenever(userSession.ongoingLoginEntry()).thenReturn(Single.just(OngoingLoginEntry(otp = "123")))
    whenever(userSession.saveOngoingLoginEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PhoneNumberTextChanged("9999"))
    uiEvents.onNext(PhoneNumberSubmitClicked())

    verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry("123", "9999"))
    verify(screen).openLoginPinScreen()
  }
}
