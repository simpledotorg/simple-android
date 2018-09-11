package org.simple.clinic.search

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.anyString
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent

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
  fun `when screen starts, the keyboard should be shown on phone number field and patient list should be setup`() {
    verify(screen).showKeyboardOnSearchEditText()
    verify(screen).setupSearchResultsList()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when search is clicked with empty name then a validation error should be shown`() {
    // TODO.
  }

  @Test
  fun `when search is clicked with empty age then a validation error should be shown`() {
    // TODO.
  }

  @Test
  fun `when search is clicked with empty name or age then patients shouldn't be searched`() {
    uiEvents.onNext(SearchQueryNameChanged("foo"))
    uiEvents.onNext(SearchQueryAgeChanged(" "))
    uiEvents.onNext(SearchClicked())

    uiEvents.onNext(SearchQueryNameChanged(""))
    uiEvents.onNext(SearchQueryAgeChanged("123"))
    uiEvents.onNext(SearchClicked())

    verify(repository, never()).searchPatientsAndPhoneNumbers(anyString(), anyInt(), anyBoolean())
    verify(screen, never()).updatePatientSearchResults(any())
  }

  @Test
  fun `when full name and age are present, and search is clicked, matching patients should be shown`() {
    val fullName = "bar"
    val age = "24"
    val matchingPatients = listOf<PatientSearchResult>(mock(), mock(), mock())
    whenever(repository.searchPatientsAndPhoneNumbers(fullName, age.toInt())).thenReturn(Observable.just(matchingPatients))

    uiEvents.onNext(SearchQueryNameChanged(fullName))
    uiEvents.onNext(SearchQueryAgeChanged(age))
    uiEvents.onNext(SearchClicked())

    verify(screen).updatePatientSearchResults(matchingPatients)
  }

  @Test
  fun `if search query is numbers, and create patient is clicked, the patient create form should open with same text in phone number field`() {
    val partialNumber = "999"
    val age = "24"

    whenever(repository.searchPatientsAndPhoneNumbers(partialNumber, age.toInt())).thenReturn(Observable.never())
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(SearchQueryNameChanged(partialNumber))
    uiEvents.onNext(SearchQueryAgeChanged(age))
    uiEvents.onNext(SearchClicked())
    uiEvents.onNext(CreateNewPatientClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).saveOngoingEntry(capture())
      assert(partialNumber == firstValue.phoneNumber!!.number)
    }
    verify(screen).openPersonalDetailsEntryScreen()
  }

  @Test
  fun `if search query is alphanumeric, and create patient is clicked, the patient create form should open with same text in full name field`() {
    val partialName = "foo"
    val age = "24"

    whenever(repository.searchPatientsAndPhoneNumbers(partialName, age.toInt())).thenReturn(Observable.never())
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(SearchQueryNameChanged(partialName))
    uiEvents.onNext(SearchQueryAgeChanged(age))
    uiEvents.onNext(SearchClicked())
    uiEvents.onNext(CreateNewPatientClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).saveOngoingEntry(capture())
      assert(partialName == firstValue.personalDetails!!.fullName)
    }
    verify(screen).openPersonalDetailsEntryScreen()
  }

  @Test
  fun `when back button is clicked, home screen should open`() {
    uiEvents.onNext(BackButtonClicked())

    verify(screen).goBackToHomeScreen()
  }

  @Test
  fun `when a patient search result is clicked, the patient's summary screen should be started`() {
    val searchResult = PatientMocker.patientSearchResult()

    uiEvents.onNext(SearchResultClicked(searchResult))

    verify(screen).openPatientSummaryScreen(searchResult.uuid)
  }
}
