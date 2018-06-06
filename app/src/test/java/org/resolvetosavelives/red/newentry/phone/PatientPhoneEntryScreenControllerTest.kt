package org.resolvetosavelives.red.newentry.phone

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.patient.OngoingPatientEntry
import org.resolvetosavelives.red.patient.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent

class PatientPhoneEntryScreenControllerTest {

  private val screen: PatientPhoneEntryScreen = mock()
  private val repository: PatientRepository = mock()
  private val dummyPhoneNumber = OngoingPatientEntry.PhoneNumber("123")

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
    val ongoingEntry = OngoingPatientEntry(phoneNumber = dummyPhoneNumber)
    whenever(repository.ongoingEntry()).thenReturn(Single.just(ongoingEntry))

    uiEvents.onNext(ScreenCreated())

    verify(screen).preFill(dummyPhoneNumber)
  }

  @Test
  fun `when screen starts and existing phone numbers are not present then they should not be pre-filled`() {
    whenever(repository.ongoingEntry()).thenReturn(Single.just(OngoingPatientEntry(phoneNumber = null)))

    uiEvents.onNext(ScreenCreated())

    verify(screen, never()).preFill(any())
  }

  @Test
  fun `when proceed is clicked then phone numbers should be saved and the next screen should be opened`() {
    whenever(repository.ongoingEntry()).thenReturn(Single.just(OngoingPatientEntry()))
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientPrimaryPhoneTextChanged(dummyPhoneNumber.number))
    uiEvents.onNext(PatientSecondaryPhoneTextChanged(dummyPhoneNumber.number))
    uiEvents.onNext(PatientPhoneEntryProceedClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).saveOngoingEntry(capture())
      assert(dummyPhoneNumber == firstValue.phoneNumber)
    }
    verify(screen).openBloodPressureEntryScreen()
  }
}
