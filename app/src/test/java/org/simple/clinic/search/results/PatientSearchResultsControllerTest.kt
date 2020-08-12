package org.simple.clinic.search.results

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class PatientSearchResultsControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui: PatientSearchResultsUi = mock()

  private val patientRepository: PatientRepository = mock()
  private val facilityRepository: FacilityRepository = mock()
  private val userSession: UserSession = mock()

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val loggedInUser = TestData.loggedInUser(UUID.fromString("e83b9b27-0a05-4750-9ef7-270cda65217b"))
  private val currentFacility = TestData.facility(UUID.fromString("af8e817c-8772-4c84-9f4f-1f331fa0b2a5"))

  private lateinit var controllerSubscription: Disposable
  private lateinit var testFixture: MobiusTestFixture<PatientSearchResultsModel, PatientSearchResultsEvent, PatientSearchResultsEffect>

  @After
  fun tearDown() {
    controllerSubscription.dispose()
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
    verify(ui).openPatientSummaryScreen(patientUuid)
    verifyNoMoreInteractions(ui)
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
    verify(ui).openLinkIdWithPatientScreen(patientUuid, identifier)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when register new patient is clicked, then patient entry screen must be opened`() {
    // given
    val fullName = "name"
    val ongoingEntry = OngoingNewPatientEntry.fromFullName(fullName)
    val searchCriteria = PatientSearchCriteria.Name(fullName)

    whenever(patientRepository.saveOngoingEntry(ongoingEntry)) doReturn (Completable.complete())

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(searchCriteria))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(ui).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(ui)
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

    whenever(patientRepository.saveOngoingEntry(ongoingEntry)) doReturn (Completable.complete())

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(searchCriteria))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(ui).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when register new patient is clicked after searching with phone number, the number must be used to create the ongoing entry`() {
    // given
    val phoneNumber = "123456"
    val ongoingEntry = OngoingNewPatientEntry.fromPhoneNumber(phoneNumber)
    val searchCriteria = PatientSearchCriteria.PhoneNumber(phoneNumber)

    whenever(patientRepository.saveOngoingEntry(ongoingEntry)) doReturn (Completable.complete())

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(searchCriteria))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(ui).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(ui)
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

    whenever(patientRepository.saveOngoingEntry(ongoingEntry)) doReturn (Completable.complete())

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(searchCriteria))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(ui).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when register new patient is clicked after switching facility, then alert facility change sheet must be opened`() {
    // given
    val fullName = "name"
    val ongoingEntry = OngoingNewPatientEntry.fromFullName(fullName)
    val searchCriteria = PatientSearchCriteria.Name(fullName)

    whenever(patientRepository.saveOngoingEntry(ongoingEntry)) doReturn (Completable.complete())

    // when
    setupController(searchCriteria)
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(searchCriteria))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(ui).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(ui)
  }

  private fun setupController(
      searchCriteria: PatientSearchCriteria
  ) {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(loggedInUser)))
    whenever(facilityRepository.currentFacility(loggedInUser)) doReturn Observable.just(currentFacility)

    val effectHandler = PatientSearchResultsEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        patientRepository = patientRepository,
        userSession = userSession,
        facilityRepository = facilityRepository,
        uiActions = ui
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

    val controller = PatientSearchResultsController(
        patientRepository = patientRepository,
        facilityRepository = facilityRepository,
        userSession = userSession,
        patientSearchCriteria = searchCriteria
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }

    uiEvents.onNext(PatientSearchResultsScreenCreated())
  }
}
