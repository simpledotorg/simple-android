package org.simple.clinic.search.results

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset.UTC

@RunWith(JUnitParamsRunner::class)
class PatientSearchResultsControllerTest {

  private val screen: PatientSearchResultsScreen = mock()
  private val repository: PatientRepository = mock()

  private lateinit var controller: PatientSearchResultsController
  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    val fixedClock = Clock.fixed(Instant.parse("2018-09-20T10:15:30.000Z"), UTC)

    controller = PatientSearchResultsController(repository, fixedClock)
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is created with age then patients matching the search query should be shown`() {
    val searchResults = listOf(PatientMocker.patientSearchResult(), PatientMocker.patientSearchResult())
    whenever(repository.search(any(), any(), any())).thenReturn(Observable.just(searchResults))

    uiEvents.onNext(PatientSearchResultsScreenCreated(PatientSearchResultsScreenKey("name", age = "23", dateOfBirth = "")))

    verify(repository).search("name", 23)
    verify(screen).updateSearchResults(searchResults)
    verify(screen).setEmptyStateVisible(false)
  }

  @Test
  @Parameters(value = [
    "25,",
    ",20/09/1993"])
  fun `when screen is created then the computed age should be shown`(age: String, dateOfBirth: String) {
    whenever(repository.search(any(), any(), any())).thenReturn(Observable.never())

    uiEvents.onNext(PatientSearchResultsScreenCreated(PatientSearchResultsScreenKey("name", age = age, dateOfBirth = dateOfBirth)))

    verify(screen).showComputedAge("25")
  }

  @Test
  fun `when screen is created with date of birth then patients matching the search query should be shown`() {
    val searchResults = listOf(PatientMocker.patientSearchResult(), PatientMocker.patientSearchResult())
    whenever(repository.search(any(), any(), any())).thenReturn(Observable.just(searchResults))

    uiEvents.onNext(PatientSearchResultsScreenCreated(PatientSearchResultsScreenKey("name", age = "", dateOfBirth = "20/09/1993")))

    verify(repository).search("name", 25)
    verify(screen).updateSearchResults(searchResults)
    verify(screen).setEmptyStateVisible(false)
  }

  @Test
  fun `when screen is created and no matching patients are available then the empty state should be shown`() {
    val emptyResults = listOf<PatientSearchResult>()
    whenever(repository.search(any(), any(), any())).thenReturn(Observable.just(emptyResults))

    uiEvents.onNext(PatientSearchResultsScreenCreated(PatientSearchResultsScreenKey("name", age = "23", dateOfBirth = "")))

    verify(repository).search("name", 23)
    verify(screen).updateSearchResults(emptyResults)
    verify(screen).setEmptyStateVisible(true)
  }

  @Test
  fun `when a patient search result is clicked, the patient's summary screen should be opened`() {
    val searchResult = PatientMocker.patientSearchResult()
    uiEvents.onNext(PatientSearchResultClicked(searchResult))

    verify(screen).openPatientSummaryScreen(searchResult.uuid)
  }

  @Test
  @Parameters(value = [
    "23,",
    ",20/09/1993"])
  fun `when create new patient is clicked then patient entry screen should be opened with prefilled name, age and date of birth`(
      age: String,
      dateOfBirth: String
  ) {
    whenever(repository.search(any(), any(), any())).thenReturn(Observable.just(listOf()))

    val preFilledEntry = OngoingPatientEntry(OngoingPatientEntry.PersonalDetails(
        fullName = "name",
        dateOfBirth = dateOfBirth,
        age = age,
        gender = null))
    whenever(repository.saveOngoingEntry(preFilledEntry)).thenReturn(Completable.complete())

    uiEvents.onNext(PatientSearchResultsScreenCreated(PatientSearchResultsScreenKey("name", age = age, dateOfBirth = dateOfBirth)))
    uiEvents.onNext(CreateNewPatientClicked())

    verify(repository).saveOngoingEntry(preFilledEntry)
    verify(screen).openPatientEntryScreen()
  }
}
