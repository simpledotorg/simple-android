package org.simple.clinic.registration.name

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent

class RegistrationFullNameScreenControllerTest {

  @get:Rule
  val rules: org.junit.rules.RuleChain = org.simple.clinic.util.Rules.global()

  val uiEvents = PublishSubject.create<UiEvent>()!!
  val screen = mock<RegistrationFullNameScreen>()
  val userSession = mock<UserSession>()
  val facilityRepository = mock<FacilityRepository>()
  val facilitySync = mock<FacilitySync>()

  private lateinit var controller: RegistrationFullNameScreenController

  @Before
  fun setUp() {
    controller = RegistrationFullNameScreenController(userSession, facilityRepository, facilitySync)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when next is clicked with a valid name then the ongoing entry should be updated with the name and the next screen should be opened`() {
    val input = "Ashok Kumar"

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(fullName = input))).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationFullNameTextChanged(input))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    verify(userSession).saveOngoingRegistrationEntry(OngoingRegistrationEntry(fullName = input))
    verify(screen).openRegistrationPinEntryScreen()
  }

  @Test
  fun `when screen is created then user's existing details should be pre-filled`() {
    val ongoingEntry = OngoingRegistrationEntry(
        fullName = "Ashok Kumar",
        phoneNumber = "1234567890")
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(facilityRepository.recordCount()).thenReturn(Observable.never())

    uiEvents.onNext(RegistrationFullNameScreenCreated())

    verify(screen).preFillUserDetails(ongoingEntry)
  }

  @Test
  fun `proceed button clicks should only be accepted if the input name is valid`() {
    val validName = "Ashok"
    val invalidName = "  "

    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(userSession.saveOngoingRegistrationEntry(OngoingRegistrationEntry(fullName = validName))).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationFullNameTextChanged(invalidName))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    uiEvents.onNext(RegistrationFullNameTextChanged(validName))
    uiEvents.onNext(RegistrationFullNameDoneClicked())

    verify(userSession, times(1)).saveOngoingRegistrationEntry(any())
    verify(screen, times(1)).openRegistrationPinEntryScreen()
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

  @Test
  fun `when screen is started and facilities haven't already been synced then facilities should be synced`() {
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(0, 10))
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(facilitySync.sync()).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationFullNameScreenCreated())

    verify(facilitySync).sync()
  }

  @Test
  fun `when screen is started and facilities have already been synced then facilities should not be synced again`() {
    whenever(facilityRepository.recordCount()).thenReturn(Observable.just(1))
    whenever(userSession.ongoingRegistrationEntry()).thenReturn(Single.just(OngoingRegistrationEntry()))
    whenever(facilitySync.sync()).thenReturn(Completable.complete())

    uiEvents.onNext(RegistrationFullNameScreenCreated())

    verify(facilitySync, never()).sync()
  }
}
