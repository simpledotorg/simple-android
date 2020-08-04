package org.simple.clinic.forgotpin.confirmpin

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.clearpatientdata.SyncAndClearPatientData
import org.simple.clinic.user.resetpin.ResetPinResult
import org.simple.clinic.user.resetpin.ResetPinResult.NetworkError
import org.simple.clinic.user.resetpin.ResetPinResult.Success
import org.simple.clinic.user.resetpin.ResetPinResult.UnexpectedError
import org.simple.clinic.user.resetpin.ResetPinResult.UserNotFound
import org.simple.clinic.user.resetpin.ResetUserPin
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class ForgotPinConfirmPinScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val ui = mock<ForgotPinConfirmPinUi>()
  private val uiActions = mock<ForgotPinConfirmPinUiActions>()
  private val userSession = mock<UserSession>()
  private val resetUserPin = mock<ResetUserPin>()
  private val syncAndClearPatientData = mock<SyncAndClearPatientData>()

  private val loggedInUser = TestData.loggedInUser(
      uuid = UUID.fromString("324d7648-e2a5-4192-831f-533b81181dc2"),
      name = "Tushar Talwar"
  )
  private val facility = TestData.facility(
      uuid = UUID.fromString("56e8780b-d0d3-4464-9f2e-ae7da54eacb9"),
      name = "PHC Obvious"
  )

  private lateinit var testFixture: MobiusTestFixture<ForgotPinConfirmPinModel, ForgotPinConfirmPinEvent, ForgotPinConfirmPinEffect>

  // FIXME 02-08-2019 : Fix tests with unexpected errors are passing even when stubs are not passed
  // We use onErrorReturn in one of the chains to wrap unexpected errors. However, when we forget
  // to stub certain calls in Mockito (UserSession#updateLoggedInStatusForUser), the tests still
  // pass (even though they shouldn't) because we are using onErrorReturn to wrap errors from
  // upstream. This needs to be fixed so that tests behave properly.

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `on start, the logged in user's full name must be shown`() {
    // when
    setupController(pin = "0000")

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verifyNoMoreInteractions(ui, uiActions)

    verifyZeroInteractions(userSession)

    verifyZeroInteractions(syncAndClearPatientData)
    verifyZeroInteractions(resetUserPin)
  }

  @Test
  fun `on start, the current selected facility should be shown`() {
    // when
    setupController(pin = "1111")

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verifyNoMoreInteractions(ui, uiActions)

    verifyNoMoreInteractions(userSession)

    verifyZeroInteractions(syncAndClearPatientData)
    verifyZeroInteractions(resetUserPin)
  }

  @Test
  fun `when submitting a PIN that does not match the previous PIN, an error should be shown`() {
    // given
    val originalPin = "0000"
    whenever(syncAndClearPatientData.run()) doReturn Completable.complete()
    whenever(resetUserPin.resetPin(originalPin)) doReturn Single.just<ResetPinResult>(Success)

    // when
    setupController(pin = originalPin)

    // then
    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("1234"))
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showPinMismatchedError()
    verifyNoMoreInteractions(ui, uiActions)

    clearInvocations(uiActions)

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("5678"))
    verify(uiActions).showPinMismatchedError()

    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when PIN is changed, any errors must be hidden`() {
    // when
    setupController(pin = "")

    uiEvents.onNext(ForgotPinConfirmPinTextChanged("1"))
    uiEvents.onNext(ForgotPinConfirmPinTextChanged("11"))
    uiEvents.onNext(ForgotPinConfirmPinTextChanged("111"))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions, times(3)).hideError()
    verifyNoMoreInteractions(ui, uiActions)

    verifyZeroInteractions(userSession)

    verifyZeroInteractions(resetUserPin)
    verifyZeroInteractions(syncAndClearPatientData)
  }

  @Test
  fun `when a valid PIN is submitted, the local patient data must be synced and then cleared`() {
    // given
    val pin = "0000"
    whenever(syncAndClearPatientData.run()) doReturn Completable.complete()

    // when
    setupController(pin = pin)

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showProgress()
    verify(uiActions).showUnexpectedError()
    verifyNoMoreInteractions(ui, uiActions)

    verify(syncAndClearPatientData).run()
    verifyNoMoreInteractions(syncAndClearPatientData)

    verify(userSession).updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)
    verifyNoMoreInteractions(userSession)

    verifyZeroInteractions(resetUserPin)
  }

  @Test
  fun `when the sync fails, it must show an unexpected error`() {
    // given
    val pin = "0000"
    whenever(syncAndClearPatientData.run()) doReturn Completable.error(RuntimeException())

    // when
    setupController(pin = pin)

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showProgress()
    verify(uiActions).showUnexpectedError()
    verifyNoMoreInteractions(ui, uiActions)

    verifyZeroInteractions(userSession)
    verifyZeroInteractions(resetUserPin)

    verify(syncAndClearPatientData).run()
    verifyNoMoreInteractions(syncAndClearPatientData)
  }

  @Test
  fun `when a valid PIN is submitted and sync succeeds, it must raise the Reset PIN request`() {
    // given
    val pin = "0000"
    whenever(resetUserPin.resetPin(pin)) doReturn Single.just<ResetPinResult>(Success)
    whenever(syncAndClearPatientData.run()) doReturn Completable.complete()
    whenever(userSession.updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)) doReturn Completable.complete()

    // when
    setupController(pin = pin)

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showProgress()
    verify(uiActions).goToHomeScreen()
    verifyNoMoreInteractions(ui, uiActions)

    verify(userSession).updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)
    verifyNoMoreInteractions(userSession)

    verify(syncAndClearPatientData).run()
    verifyNoMoreInteractions(syncAndClearPatientData)

    verify(resetUserPin).resetPin(pin)
    verifyNoMoreInteractions(resetUserPin)
  }

  @Test
  fun `when an invalid PIN is submitted, it must not attempt to raise the Reset PIN request`() {
    // when
    setupController(pin = "0000")

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("1234"))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showPinMismatchedError()
    verifyNoMoreInteractions(ui, uiActions)

    verify(resetUserPin, never()).resetPin(any())
    verifyNoMoreInteractions(resetUserPin)

    verify(syncAndClearPatientData, never()).run()
    verifyNoMoreInteractions(syncAndClearPatientData)

    verifyZeroInteractions(userSession)
  }

  @Test
  fun `when a valid PIN is submitted, the progress must be shown`() {
    // when
    val pin = "0000"
    setupController(pin = pin)

    whenever(syncAndClearPatientData.run()) doReturn Completable.complete()

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showProgress()
    verify(uiActions).showUnexpectedError()
    verifyNoMoreInteractions(ui, uiActions)

    verify(userSession).updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)
    verifyNoMoreInteractions(userSession)

    verifyZeroInteractions(resetUserPin)

    verify(syncAndClearPatientData).run()
    verifyNoMoreInteractions(syncAndClearPatientData)
  }

  @Test
  fun `when the forgot PIN call fails with a network error, the error must be shown`() {
    // given
    val pin = "0000"
    whenever(syncAndClearPatientData.run()) doReturn Completable.complete()
    whenever(userSession.updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)) doReturn Completable.complete()
    whenever(resetUserPin.resetPin(pin)) doReturn Single.just<ResetPinResult>(NetworkError)

    // when
    setupController(pin = pin)

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showProgress()
    verify(uiActions).showNetworkError()
    verifyNoMoreInteractions(ui, uiActions)

    verify(userSession).updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)
    verifyNoMoreInteractions(userSession)

    verify(resetUserPin).resetPin(pin)
    verifyNoMoreInteractions(resetUserPin)

    verify(syncAndClearPatientData).run()
    verifyNoMoreInteractions(syncAndClearPatientData)
  }

  @Test
  fun `when the forgot PIN call fails with an unexpected error, the error must be shown`() {
    // given
    val pin = "0000"
    whenever(syncAndClearPatientData.run()) doReturn Completable.complete()
    whenever(resetUserPin.resetPin(pin)) doReturn Single.just<ResetPinResult>(UnexpectedError(RuntimeException()))

    // when
    setupController(pin = pin)

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showProgress()
    verify(uiActions).showUnexpectedError()
    verifyNoMoreInteractions(ui, uiActions)

    verify(userSession).updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)
    verifyNoMoreInteractions(userSession)

    verify(syncAndClearPatientData).run()
    verifyNoMoreInteractions(syncAndClearPatientData)

    verifyZeroInteractions(resetUserPin)
  }

  @Test
  fun `when the forgot PIN call fails with a user not found error, the error must be shown`() {
    // given
    val pin = "0000"
    whenever(syncAndClearPatientData.run()) doReturn Completable.complete()
    whenever(resetUserPin.resetPin(pin)) doReturn Single.just<ResetPinResult>(UserNotFound)

    // when
    setupController(pin = pin)

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showProgress()
    verify(uiActions).showUnexpectedError()
    verifyNoMoreInteractions(ui, uiActions)

    verify(userSession).updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)
    verifyNoMoreInteractions(userSession)

    verifyZeroInteractions(resetUserPin)

    verify(syncAndClearPatientData).run()
    verifyNoMoreInteractions(syncAndClearPatientData)
  }

  @Test
  fun `when the forgot PIN call succeeds, the home screen must be opened`() {
    // given
    val pin = "0000"
    whenever(syncAndClearPatientData.run()) doReturn Completable.complete()
    whenever(userSession.updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)) doReturn Completable.complete()
    whenever(resetUserPin.resetPin(pin)) doReturn Single.just<ResetPinResult>(Success)

    // when
    setupController(pin = pin)

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked(pin))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showProgress()
    verify(uiActions).goToHomeScreen()
    verifyNoMoreInteractions(ui, uiActions)

    verify(userSession).updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)
    verifyNoMoreInteractions(userSession)

    verify(resetUserPin).resetPin(pin)
    verifyNoMoreInteractions(resetUserPin)

    verify(syncAndClearPatientData).run()
    verifyNoMoreInteractions(syncAndClearPatientData)
  }

  @Test
  fun `when resetting the PIN and data gets cleared, the user logged in status must be set to RESETTING_PIN`() {
    // given
    whenever(syncAndClearPatientData.run()) doReturn Completable.complete()

    // when
    setupController(pin = "0000")

    uiEvents.onNext(ForgotPinConfirmPinSubmitClicked("0000"))

    // then
    verify(ui).showUserName("Tushar Talwar")
    verify(ui).showFacility("PHC Obvious")
    verify(uiActions).showProgress()
    verify(uiActions).showUnexpectedError()
    verifyNoMoreInteractions(ui, uiActions)

    verify(userSession).updateLoggedInStatusForUser(loggedInUser.uuid, RESETTING_PIN)
    verifyNoMoreInteractions(userSession)

    verifyZeroInteractions(resetUserPin)

    verify(syncAndClearPatientData).run()
    verifyNoMoreInteractions(syncAndClearPatientData)
  }

  private fun setupController(pin: String) {
    val effectHandler = ForgotPinConfirmPinEffectHandler(
        userSession = userSession,
        currentUser = Lazy { loggedInUser },
        currentFacility = Lazy { facility },
        resetUserPin = resetUserPin,
        syncAndClearPatientData = syncAndClearPatientData,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    )

    val uiRenderer = ForgotPinConfirmPinUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = ForgotPinConfirmPinModel.create(previousPin = pin),
        init = ForgotPinConfirmPinInit(),
        update = ForgotPinConfirmPinUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
