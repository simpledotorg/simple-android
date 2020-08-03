package org.simple.clinic.search

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.search.PatientSearchValidationError.INPUT_EMPTY
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientSearchScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui: PatientSearchUi = mock()

  private val identifier = TestData.identifier("a8d49ec3-6945-4ef0-9358-f313e08d1579", Identifier.IdentifierType.BpPassport)

  private lateinit var controllerSubscription: Disposable
  private lateinit var testFixture: MobiusTestFixture<PatientSearchModel, PatientSearchEvent, PatientSearchEffect>
  private val uiEvents = PublishSubject.create<UiEvent>()

  private val analyticsReporter = MockAnalyticsReporter()

  @Before
  fun setUp() {
    Analytics.addReporter(analyticsReporter)
  }

  @After
  fun tearDown() {
    controllerSubscription.dispose()
    testFixture.dispose()
    Analytics.clearReporters()
  }

  @Test
  fun `when search is clicked with no input then a validation error should be shown`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(""))
    uiEvents.onNext(SearchClicked())

    // then
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verify(ui).setEmptyTextFieldErrorVisible(true)
    verify(ui).showAllPatientsInFacility()
    verify(ui).hideSearchButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when input changes then any validation error on input should be removed`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged("Anish"))

    // then
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verify(ui).hideAllPatientsInFacility()
    verify(ui).showSearchButton()
    verifyNoMoreInteractions(ui)

    clearInvocations(ui)

    // when
    uiEvents.onNext(SearchQueryTextChanged("123"))

    // then
    verify(ui).hideAllPatientsInFacility()
    verify(ui).showSearchButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when search is clicked with empty input then patients shouldn't be searched`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(""))
    uiEvents.onNext(SearchClicked())

    // then
    verify(ui, never()).openSearchResultsScreen(any())
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verify(ui).setEmptyTextFieldErrorVisible(true)
    verify(ui).showAllPatientsInFacility()
    verify(ui).hideSearchButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when a patient item is clicked, the patient summary screen should be opened`() {
    val patientUuid = UUID.fromString("7925e13f-3b04-46b0-b685-7005ebb1b6fd")

    // when
    setupController()
    uiEvents.onNext(PatientItemClicked(patientUuid))

    // then
    verify(ui).openPatientSummary(patientUuid)
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the search query is blank, the all patients list must be shown`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(""))

    // then
    verify(ui).showAllPatientsInFacility()
    verify(ui).hideSearchButton()
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the search query is blank, the search button must be hidden`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(""))

    // then
    verify(ui).hideSearchButton()
    verify(ui).showAllPatientsInFacility()
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the search query is not blank, the all patients list must be hidden`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged("a"))

    // then
    verify(ui).hideAllPatientsInFacility()
    verify(ui).showSearchButton()
    verify(ui).hideAllPatientsInFacility()
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the search query is not blank, the search button must be shown`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged("a"))

    // then
    verify(ui).showSearchButton()
    verify(ui).hideAllPatientsInFacility()
    verify(ui).hideAllPatientsInFacility()
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verifyNoMoreInteractions(ui)
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
    verify(ui).openSearchResultsScreen(PhoneNumber(expectedPhoneNumberToSearch, identifier))
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verify(ui).hideAllPatientsInFacility()
    verify(ui).showSearchButton()
    verifyNoMoreInteractions(ui)
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
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(input))
    uiEvents.onNext(SearchClicked())

    // then
    verify(ui).openSearchResultsScreen(Name(expectedNameToSearch, identifier))
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verify(ui).hideAllPatientsInFacility()
    verify(ui).showSearchButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the search query is blank, the input validation error must be reported to analytics`() {
    // when
    setupController()
    uiEvents.onNext(SearchQueryTextChanged(""))
    uiEvents.onNext(SearchClicked())

    // then
    verify(ui).hideSearchButton()
    verify(ui).showAllPatientsInFacility()
    verify(ui).setEmptyTextFieldErrorVisible(false)
    verify(ui).setEmptyTextFieldErrorVisible(true)
    verifyNoMoreInteractions(ui)

    val expectedEvent = MockAnalyticsReporter.Event("InputValidationError", mapOf("name" to INPUT_EMPTY.analyticsName))
    assertThat(analyticsReporter.receivedEvents).containsExactly(expectedEvent)
  }

  private fun setupController() {
    val effectHandler = PatientSearchEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        uiActions = ui
    )
    val uiRenderer = PatientSearchUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        update = PatientSearchUpdate(),
        effectHandler = effectHandler.build(),
        defaultModel = PatientSearchModel.create(),
        init = PatientSearchInit(),
        modelUpdateListener = uiRenderer::render
    )
    testFixture.start()

    val controller = PatientSearchScreenController(identifier)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }
  }
}
