package org.simple.clinic.search.results

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class PatientSearchResultsLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui: PatientSearchResultsUi = mock()
  private val uiActions: PatientSearchResultsUiActions = mock()

  private val patientRepository: PatientRepository = mock()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val currentFacility = TestData.facility(UUID.fromString("af8e817c-8772-4c84-9f4f-1f331fa0b2a5"))

  private lateinit var testFixture: MobiusTestFixture<PatientSearchResultsModel, PatientSearchResultsEvent, PatientSearchResultsEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when patient search result is clicked, then patient summary must be opened`() {
    // given
    val patientUuid = UUID.fromString("951ad528-1952-4840-aad6-511371736a15")
    val searchCriteria = PatientSearchCriteria.PhoneNumber("1111111111")

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultClicked(patientUuid))

    // then
    verify(uiActions).openPatientSummaryScreen(patientUuid)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when patient search result is clicked with additional identifier, then the ID must be linked with the patient`() {
    // given
    val patientUuid = UUID.fromString("951ad528-1952-4840-aad6-511371736a15")
    val identifier = TestData.identifier(value = "1a686bfd-ded2-48c6-9df6-8e61799402f6", type = Identifier.IdentifierType.BpPassport)
    val searchCriteria = PatientSearchCriteria.Name("Anish", identifier)

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultClicked(patientUuid))

    // then
    verify(uiActions).openLinkIdWithPatientScreen(patientUuid, identifier)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when register new patient is clicked, then patient entry screen must be opened`() {
    // given
    val fullName = "name"
    val ongoingEntry = OngoingNewPatientEntry.fromFullName(fullName)
    val searchCriteria = PatientSearchCriteria.Name(fullName)

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(searchCriteria))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(uiActions).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when register new patient is clicked with an additional identifier, then patient entry screen must be opened`() {
    // given
    val fullName = "name"
    val identifier = TestData.identifier(value = "1a686bfd-ded2-48c6-9df6-8e61799402f6", type = Identifier.IdentifierType.BpPassport)
    val ongoingEntry = OngoingNewPatientEntry
        .fromFullName(fullName)
        .withIdentifier(identifier)
    val searchCriteria = PatientSearchCriteria.Name(fullName, identifier)

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(searchCriteria))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(uiActions).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when register new patient is clicked after searching with phone number, the number must be used to create the ongoing entry`() {
    // given
    val phoneNumber = "123456"
    val ongoingEntry = OngoingNewPatientEntry.fromPhoneNumber(phoneNumber)
    val searchCriteria = PatientSearchCriteria.PhoneNumber(phoneNumber)

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(searchCriteria))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(uiActions).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when register new patient is clicked after searching with phone number with additional identifier, the number must be used to create the ongoing entry`() {
    // given
    val phoneNumber = "123456"
    val identifier = TestData.identifier(value = "1a686bfd-ded2-48c6-9df6-8e61799402f6", type = Identifier.IdentifierType.BpPassport)
    val ongoingEntry = OngoingNewPatientEntry
        .fromPhoneNumber(phoneNumber)
        .withIdentifier(identifier)
    val searchCriteria = PatientSearchCriteria.PhoneNumber(phoneNumber, identifier)

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(searchCriteria))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(uiActions).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when register new patient is clicked after switching facility, then alert facility change sheet must be opened`() {
    // given
    val fullName = "name"
    val ongoingEntry = OngoingNewPatientEntry.fromFullName(fullName)
    val searchCriteria = PatientSearchCriteria.Name(fullName)

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(searchCriteria))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(uiActions).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController(
      searchCriteria: PatientSearchCriteria
  ) {
    val effectHandler = PatientSearchResultsEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        patientRepository = patientRepository,
        currentFacility = dagger.Lazy { currentFacility },
        uiActions = uiActions
    )
    val uiRenderer = PatientSearchResultsUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = PatientSearchResultsModel.create(searchCriteria),
        update = PatientSearchResultsUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render,
        init = PatientSearchResultsInit()
    )
    testFixture.start()
  }
}
