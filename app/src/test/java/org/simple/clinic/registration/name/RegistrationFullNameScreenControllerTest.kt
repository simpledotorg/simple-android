package org.simple.clinic.registration.name

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.Before
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

  private lateinit var controller: RegistrationFullNameScreenController

  @Before
  fun setUp() {
    controller = RegistrationFullNameScreenController(userSession)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when next is clicked with a valid name then the ongoing entry should be updated with the name and the next screen should be opened`() {
    val input = "Ashok Kumar"
    val entryWithName = currentOngoingRegistrationEntry.withName(input)

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(currentOngoingRegistrationEntry.toOptional())

    uiEvents.onNext(RegistrationFullNameTextChanged(input))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    verify(userSession).saveOngoingRegistrationEntry(entryWithName)
    verify(screen).openRegistrationPinEntryScreen()
  }

  @Test
  fun `when screen is created then user's existing details should be pre-filled`() {
    val ongoingEntry = currentOngoingRegistrationEntry.withName("Ashok Kumar")
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(ongoingEntry.toOptional())

    uiEvents.onNext(RegistrationFullNameScreenCreated())

    verify(screen).preFillUserDetails(ongoingEntry)
  }

  @Test
  fun `proceed button clicks should only be accepted if the input name is valid`() {
    val validName = "Ashok"
    val invalidName = "  "

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(currentOngoingRegistrationEntry.toOptional())

    uiEvents.onNext(RegistrationFullNameTextChanged(invalidName))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    uiEvents.onNext(RegistrationFullNameTextChanged(validName))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    verify(screen).openRegistrationPinEntryScreen()
  }

  @Test
  fun `when proceed is clicked with an empty name then an error should be shown`() {
    uiEvents.onNext(RegistrationFullNameTextChanged(""))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    verify(screen).showEmptyNameValidationError()
    verify(userSession, never()).saveOngoingRegistrationEntry(any())
    verify(screen, never()).openRegistrationPinEntryScreen()
  }

  @Test
  fun `when input text is changed then any visible errors should be removed`() {
    uiEvents.onNext(RegistrationFullNameTextChanged(""))
    verify(screen).hideValidationError()
  }
}
