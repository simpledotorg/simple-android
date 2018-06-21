package org.simple.clinic.login.phone

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

class LoginPhoneScreenControllerTest {

  private val screen = mock<LoginPhoneScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()
  private val controller = LoginPhoneScreenController()
  private val userSession = mock<UserSession>()

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts, get OTP from intent, and create an OngoingLoginSession`() {
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
  fun `if phone number is not empty, and submit is clicked, create an ongoing login session, and go to pin screen`() {
    uiEvents.onNext(PhoneNumberTextChanged("123"))
    uiEvents.onNext(PhoneNumberSubmitClicked())

    verify(userSession).saveOngoingLoginEntry(OngoingLoginEntry("123"))
    verify(screen).openLoginOtpScreen()
  }
}
