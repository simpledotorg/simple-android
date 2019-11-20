package org.simple.clinic.registration.register

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.registeruser.RegistrationResult
import org.simple.clinic.user.registeruser.RegistrationResult.NetworkError
import org.simple.clinic.user.registeruser.RegistrationResult.Success
import org.simple.clinic.user.registeruser.RegistrationResult.UnexpectedError
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

class RegistrationLoadingScreenControllerTest {

  private val userSession = mock<UserSession>()
  private val screen = mock<RegistrationLoadingScreen>()
  private val uiEvents = PublishSubject.create<UiEvent>()

  private val controller = RegistrationLoadingScreenController(userSession)

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when retry button is clicked, then loader should show and register api should be called`() {
    whenever(userSession.register()).doReturn(Single.just<RegistrationResult>(Success))
    whenever(userSession.clearOngoingRegistrationEntry()).doReturn(Completable.complete())

    uiEvents.onNext(RegisterErrorRetryClicked)

    verify(userSession).register()
    verify(userSession).clearOngoingRegistrationEntry()
    verify(screen).openHomeScreen()
  }

  @Test
  fun `when screen is created, then the user registration api should be called`() {
    // given
    whenever(userSession.register()) doReturn Single.never()

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    verify(userSession).register()
    verifyNoMoreInteractions(userSession)
    verifyZeroInteractions(screen)
  }

  @Test
  fun `when the register user call succeeds, then clear registration entry and go to home screen`() {
    // given
    whenever(userSession.register()) doReturn Single.just<RegistrationResult>(Success)
    whenever(userSession.clearOngoingRegistrationEntry()) doReturn Completable.complete()

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    verify(screen).openHomeScreen()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the register call fails with a network error, show the network error message`() {
    // given
    whenever(userSession.register()) doReturn Single.just<RegistrationResult>(NetworkError)

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    verify(screen).showNetworkError()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the register call fails with any other error, show the generic error message`() {
    // given
    whenever(userSession.register()) doReturn Single.just<RegistrationResult>(UnexpectedError)

    // when
    uiEvents.onNext(ScreenCreated())

    // then
    verify(screen).showUnexpectedError()
    verifyNoMoreInteractions(screen)
  }
}
