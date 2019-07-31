package org.simple.clinic.addidtopatient.searchresults

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class AddIdToPatientSearchResultsControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: AddIdToPatientSearchResultsScreen = mock()

  private val patientRepository: PatientRepository = mock()
  private val controller = AddIdToPatientSearchResultsController(patientRepository)
  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when patient search result is clicked, then patient summary must be opened`() {
    val patientUuid = UUID.fromString("951ad528-1952-4840-aad6-511371736a15")

    uiEvents.onNext(AddIdToPatientSearchResultClicked(patientUuid))

    verify(screen).openPatientSummaryScreen(patientUuid)
  }

  @Test
  fun `when searching with name and register new patient is clicked, then patient entry screen must be opened with name saved in ongoing patient entry`() {
    // given
    val fullName = "name"
    val identifier = Identifier(value = "identifier", type = BpPassport)
    val ongoingEntry = OngoingNewPatientEntry
        .fromFullName(fullName)
        .withIdentifier(identifier)

    whenever(patientRepository.saveOngoingEntry(ongoingEntry))
        .thenReturn(Completable.complete())

    // when
    uiEvents.onNext(AddIdToPatientSearchResultsScreenCreated(searchCriteria = Name(fullName), identifier = identifier))
    uiEvents.onNext(AddIdToPatientSearchResultRegisterNewPatientClicked)

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(screen).openPatientEntryScreen()
  }

  @Test
  fun `when searching with phone number and register new patient is clicked, then patient entry screen must be opened with phone number saved in ongoing patient entry`() {
    // given
    val phoneNumber = "123456"
    val identifier = Identifier(value = "identifier", type = BpPassport)
    val ongoingEntry = OngoingNewPatientEntry
        .fromPhoneNumber(phoneNumber)
        .withIdentifier(identifier)

    whenever(patientRepository.saveOngoingEntry(ongoingEntry))
        .thenReturn(Completable.complete())

    // when
    uiEvents.onNext(AddIdToPatientSearchResultsScreenCreated(searchCriteria = PhoneNumber(phoneNumber), identifier = identifier))
    uiEvents.onNext(AddIdToPatientSearchResultRegisterNewPatientClicked)

    // then
    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(screen).openPatientEntryScreen()
  }
}

