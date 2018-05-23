package org.resolvetosavelives.red.newentry.phone

import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent

class PatientPhoneEntryScreenControllerTest {

  private val screen: PatientPhoneEntryScreen = mock()
  private val repository: PatientRepository = mock()
  private val dummyPhoneNumbers = OngoingPatientEntry.PhoneNumbers("123", "456")

  private lateinit var controller: PatientPhoneEntryScreenController
  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()

  @Before
  fun setUp() {
    controller = PatientPhoneEntryScreenController(repository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then the keyboard should be shown on the primary number field`() {
    whenever(repository.ongoingEntry()).thenReturn(Single.never())

    uiEvents.onNext(ScreenCreated())

    verify(screen).showKeyboardOnPrimaryPhoneNumber()
  }

  @Test
  fun `when screen starts and existing phone numbers are present then they should be pre-filled`() {
    val ongoingEntry = OngoingPatientEntry(phoneNumbers = dummyPhoneNumbers)
    whenever(repository.ongoingEntry()).thenReturn(Single.just(ongoingEntry))

    uiEvents.onNext(ScreenCreated())

    verify(screen).preFill(dummyPhoneNumbers)
  }

  @Test
  fun `when screen starts and existing phone numbers are not present then they should not be pre-filled`() {
    whenever(repository.ongoingEntry()).thenReturn(Single.just(OngoingPatientEntry(phoneNumbers = null)))

    uiEvents.onNext(ScreenCreated())

    verify(screen, never()).preFill(any())
  }

  @Test
  fun `when proceed is clicked then phone numbers should be saved and the next screen should be opened`() {
    whenever(repository.ongoingEntry()).thenReturn(Single.just(OngoingPatientEntry()))
    whenever(repository.save(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientPrimaryPhoneTextChanged(dummyPhoneNumbers.primary))
    uiEvents.onNext(PatientSecondaryPhoneTextChanged(dummyPhoneNumbers.secondary!!))
    uiEvents.onNext(PatientPhoneEntryProceedClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).save(capture())
      assert(dummyPhoneNumbers == firstValue.phoneNumbers)
    }
    verify(screen).openBloodPressureEntryScreen()
  }
}
