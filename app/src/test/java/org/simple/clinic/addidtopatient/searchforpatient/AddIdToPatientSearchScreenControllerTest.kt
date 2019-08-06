package org.simple.clinic.addidtopatient.searchforpatient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.clearInvocations
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class AddIdToPatientSearchScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: AddIdToPatientSearchScreen = mock()

  private val controller = AddIdToPatientSearchScreenController()
  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when search is clicked with empty input then a validation error should be shown`() {
    uiEvents.onNext(SearchQueryTextChanged(""))
    uiEvents.onNext(SearchClicked)

    verify(screen).setEmptySearchQueryErrorVisible(true)
  }

  @Test
  fun `when name changes then any validation error on input should be removed`() {
    uiEvents.onNext(SearchQueryTextChanged("Anish"))
    verify(screen, times(1)).setEmptySearchQueryErrorVisible(false)

    clearInvocations(screen)

    uiEvents.onNext(SearchQueryTextChanged("Anish Acharya"))
    verify(screen, times(1)).setEmptySearchQueryErrorVisible(false)
  }

  @Test
  fun `when search is clicked with empty input then patients shouldn't be searched`() {
    uiEvents.onNext(SearchQueryTextChanged(""))
    uiEvents.onNext(SearchClicked)

    verify(screen, never()).openAddIdToPatientSearchResultsScreen(any<PatientSearchCriteria>())
  }

  @Test
  fun `when a patient item is clicked, the patient summary screen should be opened`() {
    val patientUuid = UUID.fromString("7925e13f-3b04-46b0-b685-7005ebb1b6fd")

    // when
    uiEvents.onNext(PatientItemClicked(patientUuid))

    // then
    verify(screen).openPatientSummary(patientUuid)
  }

  @Test
  fun `when the search query is blank, the all patients list must be shown`() {
    // when
    uiEvents.onNext(SearchQueryTextChanged(""))

    // then
    verify(screen).showAllPatientsInFacility()
  }

  @Test
  fun `when the search query is blank, the search button must be hidden`() {
    // when
    uiEvents.onNext(SearchQueryTextChanged(""))

    // then
    verify(screen).hideSearchButton()
  }

  @Test
  fun `when the search query is not blank, the all patients list must be hidden`() {
    // when
    uiEvents.onNext(SearchQueryTextChanged("a"))

    // then
    verify(screen).hideAllPatientsInFacility()
  }

  @Test
  fun `when the search query is not blank, the search button must be shown`() {
    // when
    uiEvents.onNext(SearchQueryTextChanged("a"))

    // then
    verify(screen).showSearchButton()
  }

  @Test
  @Parameters(value = [
    "123|123",
    " 123|123",
    "123 |123",
    "  123    |123",
    "\t123|123",
    "123\t|123",
    "\t123\t|123",
    "987654321|987654321",
    "98765 12345|9876512345",
    "98765\t12345|9876512345"
  ])
  fun `when the search query is all digits and search is clicked, search by phone number must be done`(
      input: String,
      expectedPhoneNumberToSearch: String
  ) {
    // when
    uiEvents.onNext(SearchQueryTextChanged(input))
    uiEvents.onNext(SearchClicked)

    // then
    verify(screen).openAddIdToPatientSearchResultsScreen(PatientSearchCriteria.PhoneNumber(expectedPhoneNumberToSearch))
  }

  @Test
  @Parameters(value = [
    "asb|asb",
    " asb|asb",
    "asb |asb",
    "  asb   |asb",
    " anish acharya |anish acharya",
    "anish 123|anish 123",
    "123 anish|123 anish",
    "\tasb|asb",
    "asb\t|asb",
    "\t asb \t\t|asb",
    " anish acharya |anish acharya",
    "anish 123|anish 123",
    "123 anish|123 anish"
  ])
  fun `when the search query is not all digits and search is clicked, search results screen should open`(
      input: String,
      expectedNameToSearch: String
  ) {
    uiEvents.onNext(SearchQueryTextChanged(input))
    uiEvents.onNext(SearchClicked)

    verify(screen).openAddIdToPatientSearchResultsScreen(PatientSearchCriteria.Name(expectedNameToSearch))
  }
}
