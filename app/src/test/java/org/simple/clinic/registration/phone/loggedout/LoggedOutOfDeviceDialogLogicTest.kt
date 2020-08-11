package org.simple.clinic.registration.phone.loggedout

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserSession.LogoutResult.Failure
import org.simple.clinic.user.UserSession.LogoutResult.Success
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture

class LoggedOutOfDeviceDialogLogicTest {

  @get:Rule
  val rule: TestRule = RxErrorsRule()

  private val ui = mock<LoggedOutOfDeviceDialogUi>()
  private val userSession = mock<UserSession>()
  private val uiEvents = PublishSubject.create<UiEvent>()

  private lateinit var testFixture: MobiusTestFixture<LoggedOutOfDeviceModel, LoggedOutOfDeviceEvent, LoggedOutOfDeviceEffect>

  @After
  fun tearDown() {
    RxJavaPlugins.setErrorHandler(null)
    testFixture.dispose()
  }

  @Test
  fun `when the dialog is created, the okay button must be disabled`() {
    // given
    whenever(userSession.logout()).thenReturn(Single.never())

    // when
    setupController(errorHandler = null)

    // then
    verify(ui).disableOkayButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the logout result completes successfully, the okay button must be enabled`() {
    // given
    whenever(userSession.logout()).thenReturn(Single.just(Success))

    // when
    setupController(errorHandler = null)

    // then
    verify(ui).disableOkayButton()
    verify(ui).enableOkayButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the logout fails with runtime exception, then error must be thrown`() {
    // given
    var thrownError: Throwable? = null
    whenever(userSession.logout()).thenReturn(Single.just(Failure(RuntimeException())))

    // when
    setupController(errorHandler = Consumer { thrownError = it })

    // then
    verify(ui).disableOkayButton()
    verify(ui, never()).enableOkayButton()
    verifyNoMoreInteractions(ui)
    assertThat(thrownError).isNotNull()
  }

  @Test
  fun `when the logout fails with null pointer exception, then error must be thrown`() {
    // given
    var thrownError: Throwable? = null
    whenever(userSession.logout()).thenReturn(Single.just(Failure(NullPointerException())))

    // when
    setupController(errorHandler = Consumer { thrownError = it })

    // then
    verify(ui).disableOkayButton()
    verify(ui, never()).enableOkayButton()
    verifyNoMoreInteractions(ui)
    assertThat(thrownError).isNotNull()
  }

  private fun setupController(errorHandler: Consumer<Throwable>?) {
    RxJavaPlugins.setErrorHandler(errorHandler)

    val effectHandler = LoggedOutOfDeviceEffectHandler(
        userSession = userSession,
        schedulersProvider = TestSchedulersProvider.trampoline()
    )

    val uiRenderer = LoggedOutOfDeviceUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = LoggedOutOfDeviceModel.create(),
        init = LoggedOutOfDeviceInit(),
        update = LoggedOutOfDeviceUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
