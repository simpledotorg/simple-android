package org.simple.clinic.allpatientsinfacility

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class AllPatientsInFacilityModelProducerTest {
  private val defaultModel = AllPatientsInFacilityModel.FETCHING_PATIENTS
  private val modelUpdates = PublishSubject.create<AllPatientsInFacilityModel>()
  private val testObserver = modelUpdates.test()

  private val facility = PatientMocker.facility(UUID.fromString("1be5097b-1c9f-4f78-aa70-9b907f241669"))
  private val user = PatientMocker.loggedInUser()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val patientRepository = mock<PatientRepository>()

  private val effectHandler = AllPatientsInFacilityEffectHandler.createEffectHandler(
      userSession,
      facilityRepository,
      patientRepository,
      TrampolineSchedulersProvider()
  )

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

    // when
    dispatchScreenCreatedEvent()

    // then
    with(testObserver) {
      assertNoErrors()
      assertValues(defaultModel, defaultModel.facilityFetched(facility))
      assertNotTerminated()
    }
  }

  @Test
  fun `when the facility does not have any patients, then show no patients found screen`() {
    // given
    whenever(patientRepository.allPatientsInFacility(facility))
        .thenReturn(Observable.just(emptyList()))

    // when
    dispatchScreenCreatedEvent()

    // then
    val facilityFetchedState = defaultModel.facilityFetched(facility)

    with(testObserver) {
      assertNoErrors()
      assertValues(defaultModel, facilityFetchedState, facilityFetchedState.noPatients())
      assertNotTerminated()
    }
  }

  @Test
  fun `when the facility has patients, then show a list of patients`() {
    // given
    val patientSearchResults = listOf(PatientMocker.patientSearchResult())
    whenever(patientRepository.allPatientsInFacility(facility))
        .thenReturn(Observable.just(patientSearchResults))

    // when
    dispatchScreenCreatedEvent()

    // then
    val facilityFetchedState = defaultModel.facilityFetched(facility)
    val hasPatientsState = facilityFetchedState.hasPatients(patientSearchResults.map(::PatientSearchResultUiState))

    with(testObserver) {
      assertNoErrors()
      assertValues(defaultModel, facilityFetchedState, hasPatientsState)
      assertNotTerminated()
    }
  }

  @Ignore("This is automatically handled by Mobius, delete this test after manual verification.")
  @Test
  fun `when the screen is restored, then the ui change producer should emit the last known state`() {
    // given
    whenever(patientRepository.allPatientsInFacility(facility))
        .thenReturn(Observable.just(emptyList()))

    dispatchScreenCreatedEvent()

    val facilityFetchedState = defaultModel.facilityFetched(facility)
    val noPatientsState = facilityFetchedState.noPatients()

    with(testObserver) {
      assertNoErrors()
      assertValues(defaultModel, facilityFetchedState, noPatientsState)
      assertNotTerminated()
    }

    // when
    testObserver.dispose()
    val restoredTestObserver = modelUpdates.test()
    dispatchScreenRestoredEvent()

    // then
    with(restoredTestObserver) {
      assertNoErrors()
      assertValue(noPatientsState)
      assertNotTerminated()
    }
  }

  private fun dispatchScreenCreatedEvent() {
    MobiusTestFixture(
        Observable.never(),
        defaultModel,
        ::allPatientsInFacilityInit,
        ::allPatientsInFacilityUpdate,
        effectHandler,
        modelUpdates::onNext
    )
  }

  private fun dispatchScreenRestoredEvent() {
    /* not required, used only to make the tests look structurally similar */
  }
}
