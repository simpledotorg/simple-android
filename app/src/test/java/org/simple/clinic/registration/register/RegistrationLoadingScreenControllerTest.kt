package org.simple.clinic.registration.register

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.registration.RegistrationResult
import org.simple.clinic.registration.RegistrationResult.*
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class RegistrationLoadingScreenControllerTest {

  private val userSession = mock<UserSession>()
  private val screen = mock<RegistrationLoadingScreen>()
  lateinit var controller: RegistrationLoadingScreenController

  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    controller = RegistrationLoadingScreenController(userSession)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Parameters(method = "params for testing user registration")
  @Test
  fun `when screen is created then user registration api should be called`(results: RegistrationResult) {
    whenever(userSession.register()).thenReturn(Single.just(results))

    uiEvents.onNext(ScreenCreated())

    verify(userSession).register()
    when (results) {
      Success -> verify(screen).openHomeScreen()
      NetworkError -> verify(screen).showError()
      UnexpectedError -> verify(screen).showError()
    }
    verifyNoMoreInteractions(screen)
  }

  @Suppress("unused")
  fun `params for testing user registration`() =
      listOf<RegistrationResult>(
          Success,
          NetworkError,
          UnexpectedError
      )
}
