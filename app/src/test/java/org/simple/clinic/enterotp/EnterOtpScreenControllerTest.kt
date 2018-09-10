package org.simple.clinic.enterotp

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

class EnterOtpScreenControllerTest {

  private lateinit var controller: EnterOtpScreenController
  private lateinit var screen: EnterOtpScreen

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private val userSession = mock<UserSession>()

  @Before
  fun setUp() {
    screen = mock()

    controller = EnterOtpScreenController(userSession)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when the screen is created, the logged in users phone number must be shown`() {
    val user = PatientMocker.loggedInUser(phone = "1111111111")
    whenever(userSession.requireLoggedInUser()).thenReturn(Observable.just(user))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showUserPhoneNumber("1111111111")
  }

  @Test
  fun `when back is pressed, the screen must be closed`() {
    uiEvents.onNext(EnterOtpBackClicked())

    verify(screen).goBack()
  }
}
