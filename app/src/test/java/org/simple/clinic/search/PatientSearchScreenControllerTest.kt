package org.simple.clinic.search

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

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
  fun `when search text changes, matching patients should be shown`() {
    val searchQuery = "999"
    val matchingPatients = listOf<PatientSearchResult>(mock(), mock(), mock())
    whenever(repository.searchPatientsAndPhoneNumbers(searchQuery)).thenReturn(Observable.just(matchingPatients))

    uiEvents.onNext(SearchQueryTextChanged(searchQuery))
    uiEvents.onNext(SearchQueryAgeChanged(""))

    verify(screen).updatePatientSearchResults(matchingPatients)
  }

  @Test
  fun `when search text changes, and age filter is set, matching patients should be shown`() {
    val searchQuery = "foo"
    val ageFilter = "24"
    val matchingPatients = listOf<PatientSearchResult>(mock(), mock(), mock())

    whenever(repository.searchPatientsAndPhoneNumbers(searchQuery, ageFilter.toInt())).thenReturn(Observable.just(matchingPatients))

    uiEvents.onNext(SearchQueryTextChanged(searchQuery))
    uiEvents.onNext(SearchQueryAgeChanged(ageFilter))

    verify(screen).updatePatientSearchResults(matchingPatients)
  }

  @Test
  fun `if search query is numbers, and create patient is clicked, the patient create form should open with same text in phone number field`() {
    val partialNumber = "999"
    whenever(repository.searchPatientsAndPhoneNumbers(partialNumber)).thenReturn(Observable.never())
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(SearchQueryTextChanged(partialNumber))
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
    whenever(repository.searchPatientsAndPhoneNumbers(partialName)).thenReturn(Observable.never())
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(SearchQueryTextChanged(partialName))
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
    val patientUuid = UUID.randomUUID()

    // TODO: Move to PatientMocker.
    val searchResult = PatientSearchResult(
        uuid = patientUuid,
        fullName = "Ashok Kumar",
        gender = mock(),
        dateOfBirth = null,
        age = mock(),
        status = mock(),
        createdAt = mock(),
        updatedAt = mock(),
        address = mock(),
        syncStatus = mock(),
        phoneNumber = "3.14159",
        phoneType = mock(),
        phoneUuid = mock(),
        phoneActive = true,
        phoneCreatedAt = mock(),
        phoneUpdatedAt = mock())

    uiEvents.onNext(SearchResultClicked(searchResult))

    verify(screen).openPatientSummaryScreen(patientUuid)
  }
}
