package org.simple.clinic.registration.name

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

class RegistrationFullNameScreenControllerTest {

  val uiEvents = PublishSubject.create<UiEvent>()!!
  val screen = mock<RegistrationFullNameScreen>()
  val userSession = mock<UserSession>()

  private lateinit var controller: RegistrationFullNameScreenController

  @Before
  fun setUp() {
    controller = RegistrationFullNameScreenController(userSession)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when next button is clicked then ongoing entry should be updated with the input full name and the next screen should be opened`() {
    val input = "Ashok Kumar"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(fullName = input))).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationFullNameTextChanged(input))
    uiEvents.onNext(RegistrationFullNameNextClicked())

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(fullName = input))
    verify(screen).openRegistrationNameEntryScreen()
  }

  @Test
  fun `while full name field is empty then the next button should remain disabled`() {
    uiEvents.onNext(RegistrationFullNameTextChanged(""))
    uiEvents.onNext(RegistrationFullNameTextChanged("A"))
    uiEvents.onNext(RegistrationFullNameTextChanged("As"))
    uiEvents.onNext(RegistrationFullNameTextChanged(""))

    verify(screen, times(2)).setNextButtonEnabled(false)
    verify(screen, times(1)).setNextButtonEnabled(true)
  }
}
