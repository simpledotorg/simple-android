package org.resolvetosavelives.red.newentry.mobile

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
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent

class PatientMobileEntryScreenControllerTest {

  private val screen: PatientMobileEntryScreen = mock()
  private val repository: PatientRepository = mock()
  private val dummyMobileNumbers = OngoingPatientEntry.MobileNumbers("123", "456")

  private lateinit var controller: PatientMobileEntryScreenController
  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()

  @Before
  fun setUp() {
    controller = PatientMobileEntryScreenController(repository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then the keyboard should be shown on the primary number field`() {
    whenever(repository.ongoingEntry()).thenReturn(Single.never())

    uiEvents.onNext(ScreenCreated())

    verify(screen).showKeyboardOnPrimaryMobileNumber()
  }

  @Test
  fun `when screen starts and existing mobile numbers are present then existing details should be pre-filled`() {
    val ongoingEntry = OngoingPatientEntry(mobileNumbers = dummyMobileNumbers)
    whenever(repository.ongoingEntry()).thenReturn(Single.just(ongoingEntry))

    uiEvents.onNext(ScreenCreated())

    verify(screen).preFill(dummyMobileNumbers)
  }

  @Test
  fun `when screen starts and existing mobile numbers are not present then existing details should not be pre-filled`() {
    whenever(repository.ongoingEntry()).thenReturn(Single.just(OngoingPatientEntry(mobileNumbers = null)))

    uiEvents.onNext(ScreenCreated())

    verify(screen, never()).preFill(any())
  }

  @Test
  fun `when proceed is clicked then mobile numbers should be saved and the next screen should be opened`() {
    whenever(repository.ongoingEntry()).thenReturn(Single.just(OngoingPatientEntry()))
    whenever(repository.save(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientPrimaryMobileTextChanged(dummyMobileNumbers.primary))
    uiEvents.onNext(PatientSecondaryMobileTextChanged(dummyMobileNumbers.secondary!!))
    uiEvents.onNext(PatientMobileEntryProceedClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).save(capture())
      assert(dummyMobileNumbers == firstValue.mobileNumbers)
    }
    verify(screen).openBloodPressureEntryScreen()
  }
}
