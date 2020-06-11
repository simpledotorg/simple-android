package org.simple.clinic.registration.pin

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class RegistrationPinScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<RegistrationPinUi>()
  private val userSession = mock<UserSession>()

  private val ongoingRegistrationEntry = OngoingRegistrationEntry(
      uuid = UUID.fromString("db466606-9c0d-4f5f-9283-3c3c4f9b37a7"),
      fullName = "Anish Acharya"
  )

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when 4 digits are entered then the PIN should be submitted automatically`() {
    // when
    setupController()
    uiEvents.onNext(RegistrationPinTextChanged("1"))
    uiEvents.onNext(RegistrationPinTextChanged("12"))
    uiEvents.onNext(RegistrationPinTextChanged("123"))
    uiEvents.onNext(RegistrationPinTextChanged("1234"))

    // then
    verify(userSession).ongoingRegistrationEntry()
    verify(userSession).saveOngoingRegistrationEntry(ongoingRegistrationEntry.withPin("1234"))
    verify(ui).hideIncompletePinError()
    verify(ui).openRegistrationConfirmPinScreen()
    verifyNoMoreInteractions(ui, userSession)
  }

  @Test
  fun `when next button is clicked then ongoing entry should be updated with the input PIN and the next screen should be opened`() {
    // given
    val input = "1234"

    // when
    setupController()
    uiEvents.onNext(RegistrationPinTextChanged(input))

    // then
    verify(userSession).ongoingRegistrationEntry()
    verify(userSession).saveOngoingRegistrationEntry(ongoingRegistrationEntry.withPin(input))
    verify(ui).openRegistrationConfirmPinScreen()
    verify(ui).hideIncompletePinError()
    verifyNoMoreInteractions(ui, userSession)
  }

  @Test
  fun `proceed button clicks should only be accepted if the input PIN is of 4 digits`() {
    // given
    val validPin = "1234"
    val invalidPin = "1"

    // when
    setupController()
    uiEvents.onNext(RegistrationPinTextChanged(invalidPin))
    uiEvents.onNext(RegistrationPinTextChanged(validPin))

    // then
    verify(userSession).ongoingRegistrationEntry()
    verify(userSession).saveOngoingRegistrationEntry(ongoingRegistrationEntry.withPin(validPin))
    verify(ui).openRegistrationConfirmPinScreen()
    verify(ui).hideIncompletePinError()
    verifyNoMoreInteractions(ui, userSession)
  }

  @Test
  fun `when proceed is clicked with a PIN of length less than 4 digits then an error should be shown`() {
    // when
    setupController()
    uiEvents.onNext(RegistrationPinTextChanged("123"))
    uiEvents.onNext(RegistrationPinDoneClicked())

    // then
    verify(userSession, never()).ongoingRegistrationEntry()
    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(ui).showIncompletePinError()
    verify(ui, never()).openRegistrationConfirmPinScreen()
    verify(ui).hideIncompletePinError()
    verifyNoMoreInteractions(ui, userSession)
  }

  @Test
  fun `when the PIN is submitted then any visible errors should be removed`() {
    // when
    setupController()
    uiEvents.onNext(RegistrationPinDoneClicked())

    // then
    verify(ui).hideIncompletePinError()
    verifyNoMoreInteractions(ui, userSession)
  }

  private fun setupController(
      ongoingRegistrationEntry: OngoingRegistrationEntry = this.ongoingRegistrationEntry
  ) {
    whenever(userSession.ongoingRegistrationEntry()) doReturn Just(ongoingRegistrationEntry)

    val controller = RegistrationPinScreenController(userSession)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }
  }
}
