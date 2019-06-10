package org.simple.clinic.registration.phone.loggedout

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserSession.LogoutResult.Failure
import org.simple.clinic.user.UserSession.LogoutResult.Success
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class LoggedOutOfDeviceDialogControllerTest {

  val dialog = mock<LoggedOutOfDeviceDialog>()
  val userSession = mock<UserSession>()
  val uiEvents = PublishSubject.create<UiEvent>()

  val controller = LoggedOutOfDeviceDialogController(userSession)

  @Before
  fun setUp() {
    RxJavaPlugins.setErrorHandler(null)
    uiEvents
        .compose(controller)
        .subscribe({ uiChange -> uiChange(dialog) }, { throw it })
  }

  @Test
  @Parameters(method = "params for enabling okay button")
  fun `when the logout result completes successfully, the okay button must be enabled`(
      logoutResult: UserSession.LogoutResult,
      shouldEnableButton: Boolean
  ) {
    var thrownError: Throwable? = null
    RxJavaPlugins.setErrorHandler { thrownError = it }
    whenever(userSession.logout()).thenReturn(Single.just(logoutResult))

    uiEvents.onNext(ScreenCreated())

    if (shouldEnableButton) {
      verify(dialog).enableOkayButton()
      assertThat(thrownError).isNull()
    } else {
      verify(dialog, never()).enableOkayButton()
      assertThat(thrownError).isNotNull()
    }
  }

  @Suppress("Unused")
  fun `params for enabling okay button`(): List<List<Any>> {
    fun testCase(
        logoutResult: UserSession.LogoutResult,
        shouldEnableButton: Boolean
    ): List<Any> {
      return listOf(logoutResult, shouldEnableButton)
    }

    return listOf(
        testCase(
            logoutResult = Success,
            shouldEnableButton = true
        ),
        testCase(
            logoutResult = Failure(RuntimeException()),
            shouldEnableButton = false
        ),
        testCase(
            logoutResult = Failure(NullPointerException()),
            shouldEnableButton = false
        )
    )
  }
}
