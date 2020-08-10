package org.simple.clinic.registration.phone.loggedout

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserSession.LogoutResult.Failure
import org.simple.clinic.user.UserSession.LogoutResult.Success
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class LoggedOutOfDeviceDialogControllerTest {

  @get:Rule
  val rule: TestRule = RxErrorsRule()

  private val dialog = mock<LoggedOutOfDeviceDialog>()
  private val userSession = mock<UserSession>()
  private val uiEvents = PublishSubject.create<UiEvent>()

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when the dialog is created, the okay button must be disabled`() {
    // given
    RxJavaPlugins.setErrorHandler(null)
    whenever(userSession.logout()).thenReturn(Single.never())

    // when
    setupController()
    uiEvents.onNext(ScreenCreated())

    // then
    verify(dialog).disableOkayButton()
  }

  @Test
  fun `when the logout result completes successfully, the okay button must be enabled`() {
    // given
    RxJavaPlugins.setErrorHandler(null)
    whenever(userSession.logout()).thenReturn(Single.just(Success))

    // when
    setupController()
    uiEvents.onNext(ScreenCreated())

    // then
    verify(dialog).enableOkayButton()
  }

  @Test
  @Parameters(method = "params for logout result failures")
  fun `when the logout fails, the error must be thrown`(logoutResult: UserSession.LogoutResult) {
    // given
    var thrownError: Throwable? = null
    RxJavaPlugins.setErrorHandler { thrownError = it }
    whenever(userSession.logout()).thenReturn(Single.just(logoutResult))

    // when
    setupController()
    uiEvents.onNext(ScreenCreated())

    // then
    verify(dialog, never()).enableOkayButton()
    assertThat(thrownError).isNotNull()
  }

  @Suppress("Unused")
  fun `params for logout result failures`(): List<UserSession.LogoutResult> {
    return listOf(
        Failure(RuntimeException()),
        Failure(NullPointerException())
    )
  }

  private fun setupController() {
    val controller = LoggedOutOfDeviceDialogController(userSession)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe({ uiChange -> uiChange(dialog) }, { throw it })
  }
}
