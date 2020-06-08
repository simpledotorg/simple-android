package org.simple.clinic.registration.name

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
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
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class RegistrationFullNameScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()!!
  private val screen = mock<RegistrationFullNameScreen>()
  private val userSession = mock<UserSession>()

  private val currentOngoingRegistrationEntry = OngoingRegistrationEntry(
      uuid = UUID.fromString("301a9ea3-caed-4e50-a144-bc5aad66a53d"),
      phoneNumber = "1111111111"
  )

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when next is clicked with a valid name then the ongoing entry should be updated with the name and the next screen should be opened`() {
    // given
    val input = "Ashok Kumar"
    val entryWithName = currentOngoingRegistrationEntry.withName(input)

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(currentOngoingRegistrationEntry.toOptional())

    // when
    setupController()
    uiEvents.onNext(RegistrationFullNameTextChanged(input))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    // then
    verify(userSession).saveOngoingRegistrationEntry(entryWithName)
    verify(screen).openRegistrationPinEntryScreen()
    verify(screen).preFillUserDetails(currentOngoingRegistrationEntry)
    verify(screen).hideValidationError()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when screen is created then user's existing details should be pre-filled`() {
    // given
    val ongoingEntry = currentOngoingRegistrationEntry.withName("Ashok Kumar")
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingEntry.toOptional())

    // when
    setupController(ongoingRegistrationEntry = ongoingEntry)

    // then
    verify(screen).preFillUserDetails(ongoingEntry)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `proceed button clicks should only be accepted if the input name is valid`() {
    // given
    val validName = "Ashok"
    val invalidName = "  "

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(currentOngoingRegistrationEntry.toOptional())

    // when
    setupController()
    verify(screen).preFillUserDetails(currentOngoingRegistrationEntry)
    uiEvents.onNext(RegistrationFullNameTextChanged(invalidName))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    // then
    verify(screen).hideValidationError()
    verify(screen).showEmptyNameValidationError()
    clearInvocations(screen)

    // when
    uiEvents.onNext(RegistrationFullNameTextChanged(validName))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    // then
    verify(userSession).saveOngoingRegistrationEntry(currentOngoingRegistrationEntry.withName(validName))
    verify(screen).openRegistrationPinEntryScreen()
    verify(screen).hideValidationError()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when proceed is clicked with an empty name then an error should be shown`() {
    // when
    setupController()
    uiEvents.onNext(RegistrationFullNameTextChanged(""))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    // then
    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(screen).preFillUserDetails(currentOngoingRegistrationEntry)
    verify(screen).hideValidationError()
    verify(screen).showEmptyNameValidationError()
    verify(screen, never()).openRegistrationPinEntryScreen()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when input text is changed then any visible errors should be removed`() {
    // when
    setupController()
    uiEvents.onNext(RegistrationFullNameTextChanged(""))

    // then
    verify(screen).preFillUserDetails(currentOngoingRegistrationEntry)
    verify(screen).hideValidationError()
    verifyNoMoreInteractions(screen)
  }

  private fun setupController(
      ongoingRegistrationEntry: OngoingRegistrationEntry = currentOngoingRegistrationEntry
  ) {
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingRegistrationEntry.toOptional())

    val controller = RegistrationFullNameScreenController(userSession)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    uiEvents.onNext(RegistrationFullNameScreenCreated())
  }
}
