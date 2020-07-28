package org.simple.clinic.search.results

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientSearchResultsControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: PatientSearchResultsScreen = mock()

  private val patientRepository: PatientRepository = mock()
  private val facilityRepository: FacilityRepository = mock()
  private val userSession: UserSession = mock()

  private val controller = PatientSearchResultsController(patientRepository, facilityRepository, userSession)
  private val uiEvents = PublishSubject.create<UiEvent>()


  private val loggedInUser = TestData.loggedInUser(UUID.fromString("e83b9b27-0a05-4750-9ef7-270cda65217b"))
  private val currentFacility = TestData.facility(UUID.fromString("af8e817c-8772-4c84-9f4f-1f331fa0b2a5"))

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }

    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(loggedInUser)))
    whenever(facilityRepository.currentFacility(loggedInUser)) doReturn Observable.just(currentFacility)
  }

  @Test
  fun `when patient search result is clicked, then patient summary must be opened`() {
    val patientUuid = UUID.fromString("951ad528-1952-4840-aad6-511371736a15")

    uiEvents.onNext(PatientSearchResultClicked(patientUuid))

    verify(screen).openPatientSummaryScreen(patientUuid)
  }

  @Test
  fun `when register new patient is clicked, then patient entry screen must be opened`() {
    // given
    val fullName = "name"
    val ongoingEntry = OngoingNewPatientEntry.fromFullName(fullName)

    whenever(patientRepository.saveOngoingEntry(ongoingEntry)) doReturn (Completable.complete())

    // when
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(PatientSearchCriteria.Name(fullName)))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(screen).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when register new patient is clicked with an additional identifier, then patient entry screen must be opened`() {
    // given
    val fullName = "name"
    val identifier = TestData.identifier(value = "1a686bfd-ded2-48c6-9df6-8e61799402f6", type = Identifier.IdentifierType.BpPassport)
    val ongoingEntry = OngoingNewPatientEntry
        .fromFullName(fullName)
        .withIdentifier(identifier)

    whenever(patientRepository.saveOngoingEntry(ongoingEntry)) doReturn (Completable.complete())

    // when
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(PatientSearchCriteria.Name(fullName, identifier)))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(screen).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when register new patient is clicked after searching with phone number, the number must be used to create the ongoing entry`() {
    // given
    val phoneNumber = "123456"
    val ongoingEntry = OngoingNewPatientEntry.fromPhoneNumber(phoneNumber)

    whenever(patientRepository.saveOngoingEntry(ongoingEntry)) doReturn (Completable.complete())

    // when
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(PatientSearchCriteria.PhoneNumber(phoneNumber)))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(screen).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when register new patient is clicked after searching with phone number with additional identifier, the number must be used to create the ongoing entry`() {
    // given
    val phoneNumber = "123456"
    val identifier = TestData.identifier(value = "1a686bfd-ded2-48c6-9df6-8e61799402f6", type = Identifier.IdentifierType.BpPassport)
    val ongoingEntry = OngoingNewPatientEntry
        .fromPhoneNumber(phoneNumber)
        .withIdentifier(identifier)

    whenever(patientRepository.saveOngoingEntry(ongoingEntry)) doReturn (Completable.complete())

    // when
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(PatientSearchCriteria.PhoneNumber(phoneNumber, identifier)))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(screen).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when register new patient is clicked after switching facility, then alert facility change sheet must be opened`() {
    // given
    val fullName = "name"
    val ongoingEntry = OngoingNewPatientEntry.fromFullName(fullName)

    whenever(patientRepository.saveOngoingEntry(ongoingEntry)) doReturn (Completable.complete())

    // when
    uiEvents.onNext(PatientSearchResultRegisterNewPatient(PatientSearchCriteria.Name(fullName)))

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(screen).openPatientEntryScreen(currentFacility)
    verifyNoMoreInteractions(screen)
  }
}
