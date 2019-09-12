package org.simple.clinic.allpatientsinfacility

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilityUi
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilityUiChangeProducer
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilityUiState
import org.simple.clinic.allpatientsinfacility_old.FacilityUiState
import org.simple.clinic.allpatientsinfacility_old.PatientSearchResultUiState
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class AllPatientsInFacilityUiChangeProducerTest {
  private val view = mock<AllPatientsInFacilityUi>()
  private val uiChangeProducer = AllPatientsInFacilityUiChangeProducer(TrampolineSchedulersProvider())
  private val viewStateSubject = PublishSubject.create<AllPatientsInFacilityUiState>()

  private val facility = PatientMocker.facility(UUID.fromString("1be5097b-1c9f-4f78-aa70-9b907f241669"))

  private lateinit var disposable: Disposable

  @Before
  fun setUp() {
    disposable = viewStateSubject
        .compose(uiChangeProducer)
        .subscribe { uiChange -> uiChange(view) }
  }

  @After
  fun tearDown() {
    disposable.dispose()
  }

  @Test
  fun `when a facility is being fetched, then do nothing`() {
    // when
    viewStateSubject.onNext(AllPatientsInFacilityUiState.FETCHING_PATIENTS)

    // then
    verifyZeroInteractions(view)
  }

  @Test
  fun `when a facility is fetched, then do nothing`() {
    // given
    val fetchingPatientsState = AllPatientsInFacilityUiState.FETCHING_PATIENTS
        .facilityFetched(facility)

    // when
    viewStateSubject.onNext(fetchingPatientsState)

    // then
    verifyZeroInteractions(view)
  }

  @Test
  fun `when a facility has no patients, then show no patients found in facility`() {
    // given
    val noPatientsFoundInFacilityState = AllPatientsInFacilityUiState.FETCHING_PATIENTS
        .facilityFetched(facility)
        .noPatients()

    // when
    viewStateSubject.onNext(noPatientsFoundInFacilityState)

    // then
    verify(view).showNoPatientsFound(noPatientsFoundInFacilityState.facilityUiState!!.name)
    verifyNoMoreInteractions(view)
  }

  @Test
  fun `when a facility has patients, then show the patients search result list`() {
    // given
    val patientSearchResults = listOf(PatientMocker.patientSearchResult())
        .map(::PatientSearchResultUiState)

    val hasPatientsInFacilityState = AllPatientsInFacilityUiState.FETCHING_PATIENTS
        .facilityFetched(facility)
        .hasPatients(patientSearchResults)

    // when
    viewStateSubject.onNext(hasPatientsInFacilityState)

    // then
    verify(view).showPatients(FacilityUiState(facility.uuid, facility.name), patientSearchResults)
    verifyNoMoreInteractions(view)
  }
}
