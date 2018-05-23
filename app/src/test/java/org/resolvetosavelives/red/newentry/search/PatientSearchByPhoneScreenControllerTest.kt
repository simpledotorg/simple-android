package org.resolvetosavelives.red.newentry.search

import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.widgets.UiEvent

class PatientSearchByPhoneScreenControllerTest {

  private val screen: PatientSearchByPhoneScreen = mock()
  private val repository: PatientRepository = mock()

  private lateinit var controller: PatientSearchByPhoneScreenController
  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    controller = PatientSearchByPhoneScreenController(repository)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then the keyboard should be shown on phone number field and patient list should be setup`() {
    verify(screen).showKeyboardOnPhoneNumberField()
    verify(screen).setupSearchResultsList()
  }

  @Test
  fun `when phone number text changes then matching patients should be shown`() {
    val partialNumber = "999"
    val matchingPatients = listOf<Patient>(mock(), mock(), mock())
    whenever(repository.search(partialNumber)).thenReturn(Observable.just(matchingPatients))

    uiEvents.onNext(PatientPhoneNumberTextChanged(partialNumber))

    verify(screen).updatePatientSearchResults(matchingPatients)
  }

  @Test
  fun `when new patient is clicked then the manual entry flow should be started`() {
    val partialNumber = "999"
    whenever(repository.search(partialNumber)).thenReturn(Observable.never())
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientPhoneNumberTextChanged(partialNumber))
    uiEvents.onNext(PatientSearchByPhoneProceedClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).saveOngoingEntry(capture())
      assertEquals(partialNumber, firstValue.phoneNumbers!!.primary)
    }
    verify(screen).openPersonalDetailsEntryScreen()
  }
}
