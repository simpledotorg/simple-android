package org.simple.clinic.search

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientSearchScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: PatientSearchScreen = mock()

  private val identifier = TestData.identifier("a8d49ec3-6945-4ef0-9358-f313e08d1579", Identifier.IdentifierType.BpPassport)

  private lateinit var controllerSubscription: Disposable
  private val uiEvents = PublishSubject.create<UiEvent>()

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when search is clicked with no input then a validation error should be shown`() {
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(""))
    uiEvents.onNext(SearchClicked())

    verify(screen, times(1)).setEmptyTextFieldErrorVisible(true)
  }

  @Test
  fun `when input changes then any validation error on input should be removed`() {
    setupController()
    uiEvents.onNext(SearchQueryTextChanged("Anish"))
    verify(screen).setEmptyTextFieldErrorVisible(false)

    clearInvocations(screen)

    uiEvents.onNext(SearchQueryTextChanged("123"))
    verify(screen).setEmptyTextFieldErrorVisible(false)
  }

  @Test
  fun `when search is clicked with empty input then patients shouldn't be searched`() {
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(""))
    uiEvents.onNext(SearchClicked())

    verify(screen, never()).openSearchResultsScreen(any())
  }

  @Test
  fun `when a patient item is clicked, the patient summary screen should be opened`() {
    val patientUuid = UUID.fromString("7925e13f-3b04-46b0-b685-7005ebb1b6fd")

    // when
    setupController()
    uiEvents.onNext(PatientItemClicked(patientUuid))

    // then
    verify(screen).openPatientSummary(patientUuid)
  }

  @Test
  fun `when the search query is blank, the all patients list must be shown`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(""))

    // then
    verify(screen).showAllPatientsInFacility()
  }

  @Test
  fun `when the search query is blank, the search button must be hidden`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(""))

    // then
    verify(screen).hideSearchButton()
  }

  @Test
  fun `when the search query is not blank, the all patients list must be hidden`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged("a"))

    // then
    verify(screen).hideAllPatientsInFacility()
  }

  @Test
  fun `when the search query is not blank, the search button must be shown`() {
    // when
    setupController()
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
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(input))
    uiEvents.onNext(SearchClicked())

    // then
    verify(screen).openSearchResultsScreen(PhoneNumber(expectedPhoneNumberToSearch, identifier))
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
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(input))
    uiEvents.onNext(SearchClicked())

    verify(screen).openSearchResultsScreen(Name(expectedNameToSearch, identifier))
  }

  private fun setupController() {
    val controller = PatientSearchScreenController(identifier)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }
}
