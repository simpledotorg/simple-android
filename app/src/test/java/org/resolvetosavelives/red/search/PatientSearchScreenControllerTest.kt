package org.resolvetosavelives.red.search

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.patient.OngoingPatientEntry
import org.resolvetosavelives.red.patient.Patient
import org.resolvetosavelives.red.patient.PatientRepository
import org.resolvetosavelives.red.widgets.UiEvent

class PatientSearchScreenControllerTest {

  private val screen: PatientSearchScreen = mock()
  private val repository: PatientRepository = mock()

  private lateinit var controller: PatientSearchScreenController
  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    controller = PatientSearchScreenController(repository)

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
    whenever(repository.searchPatients(partialNumber)).thenReturn(Observable.just(matchingPatients))

    uiEvents.onNext(PatientPhoneNumberTextChanged(partialNumber))

    verify(screen).updatePatientSearchResults(matchingPatients)
  }

  @Test
  fun `when new patient is clicked then the manual entry flow should be started`() {
    val partialNumber = "999"
    whenever(repository.searchPatients(partialNumber)).thenReturn(Observable.never())
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientPhoneNumberTextChanged(partialNumber))
    uiEvents.onNext(PatientSearchProceedClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).saveOngoingEntry(capture())
      assertEquals(partialNumber, firstValue.phoneNumber!!.number)
    }
    verify(screen).openPersonalDetailsEntryScreen()
  }
}
