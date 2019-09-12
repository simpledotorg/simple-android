package org.simple.clinic.allpatientsinfacility

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilityUiState
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilityUiStateProducer
import org.simple.clinic.allpatientsinfacility_old.PatientSearchResultUiState
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenRestored
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class AllPatientsInFacilityUiStateProducerTest {
  private val initialState = AllPatientsInFacilityUiState.FETCHING_PATIENTS
  private val facility = PatientMocker.facility(UUID.fromString("1be5097b-1c9f-4f78-aa70-9b907f241669"))
  private val user = PatientMocker.loggedInUser()

  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val patientRepository = mock<PatientRepository>()

  private val uiStateProducer = AllPatientsInFacilityUiStateProducer(
      userSession,
      facilityRepository,
      patientRepository,
      TrampolineSchedulersProvider()
  )
  private val uiEventsSubject = PublishSubject.create<UiEvent>()
  private val uiStates = uiEventsSubject
      .compose(uiStateProducer)
      .doOnNext { uiStateProducer.states.onNext(it) }

  @Before
  fun setUp() {
    whenever(userSession.requireLoggedInUser())
        .thenReturn(Observable.just(user))
    whenever(facilityRepository.currentFacility(user))
        .thenReturn(Observable.just(facility))
  }

  @Test
  fun `when the screen is created, then current facility must be fetched`() {
    // given
    whenever(patientRepository.allPatientsInFacility(facility))
        .thenReturn(Observable.never())

    val testObserver = uiStates.test()

    // when
    uiEventsSubject.onNext(ScreenCreated())

    // then
    with(testObserver) {
      assertNoErrors()
      assertValues(initialState, initialState.facilityFetched(facility))
      assertNotTerminated()
    }
  }

  @Test
  fun `when the facility does not have any patients, then show no patients found screen`() {
    // given
    whenever(patientRepository.allPatientsInFacility(facility))
        .thenReturn(Observable.just(emptyList()))

    val testObserver = uiStates.test()

    // when
    uiEventsSubject.onNext(ScreenCreated())

    // then
    val facilityFetchedState = initialState.facilityFetched(facility)

    with(testObserver) {
      assertNoErrors()
      assertValues(initialState, facilityFetchedState, facilityFetchedState.noPatients())
      assertNotTerminated()
    }
  }

  @Test
  fun `when the facility has patients, then show a list of patients`() {
    // given
    val patientSearchResults = listOf(PatientMocker.patientSearchResult())
    whenever(patientRepository.allPatientsInFacility(facility))
        .thenReturn(Observable.just(patientSearchResults))

    val testObserver = uiStates.test()

    // when
    uiEventsSubject.onNext(ScreenCreated())

    // then
    val facilityFetchedState = initialState.facilityFetched(facility)
    val hasPatientsState = facilityFetchedState.hasPatients(patientSearchResults.map(::PatientSearchResultUiState))

    with(testObserver) {
      assertNoErrors()
      assertValues(initialState, facilityFetchedState, hasPatientsState)
      assertNotTerminated()
    }
  }

  @Test
  fun `when the screen is restored, then the ui change producer should emit the last known state`() {
    // given
    whenever(patientRepository.allPatientsInFacility(facility))
        .thenReturn(Observable.just(emptyList()))

    val testObserver = uiStates.test()

    uiEventsSubject.onNext(ScreenCreated())

    val facilityFetchedState = initialState.facilityFetched(facility)
    val noPatientsState = facilityFetchedState.noPatients()

    with(testObserver) {
      assertNoErrors()
      assertValues(initialState, facilityFetchedState, noPatientsState)
      assertNotTerminated()
    }

    // when
    testObserver.dispose()
    val restoredTestObserver = uiStates.test()
    uiEventsSubject.onNext(ScreenRestored)

    // then
    with(restoredTestObserver) {
      assertNoErrors()
      assertValue(noPatientsState)
      assertNotTerminated()
    }
  }
}
