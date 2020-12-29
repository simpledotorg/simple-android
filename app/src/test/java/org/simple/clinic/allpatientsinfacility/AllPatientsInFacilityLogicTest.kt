package org.simple.clinic.allpatientsinfacility

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class AllPatientsInFacilityLogicTest {
  private val defaultModel = AllPatientsInFacilityModel.FETCHING_PATIENTS
  private val modelUpdates = PublishSubject.create<AllPatientsInFacilityModel>()
  private val testObserver = modelUpdates.test()

  private val facility = TestData.facility(UUID.fromString("1be5097b-1c9f-4f78-aa70-9b907f241669"))
  private val facilityRepository = mock<FacilityRepository>()
  private val patientRepository = mock<PatientRepository>()

  private val effectHandler = AllPatientsInFacilityEffectHandler.createEffectHandler(
      facilityRepository,
      patientRepository,
      TrampolineSchedulersProvider()
  )

  private lateinit var fixture: MobiusTestFixture<AllPatientsInFacilityModel, AllPatientsInFacilityEvent, AllPatientsInFacilityEffect>

  @Before
  fun setUp() {
    whenever(facilityRepository.currentFacility())
        .thenReturn(Observable.just(facility))

    fixture = MobiusTestFixture(
        Observable.never(),
        defaultModel,
        AllPatientsInFacilityInit(),
        AllPatientsInFacilityUpdate(),
        effectHandler,
        modelUpdates::onNext
    )
  }

  @After
  fun teardown() {
    fixture.dispose()
  }

  @Test
  fun `when the screen is created, then current facility must be fetched`() {
    // given
    whenever(patientRepository.allPatientsInFacility_Old(facility))
        .thenReturn(Observable.never())

    // when
    screenCreated()

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
    whenever(patientRepository.allPatientsInFacility_Old(facility))
        .thenReturn(Observable.just(emptyList()))

    // when
    screenCreated()

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
    val patientSearchResults = listOf(TestData.patientSearchResult())
    whenever(patientRepository.allPatientsInFacility_Old(facility))
        .thenReturn(Observable.just(patientSearchResults))

    // when
    screenCreated()

    // then
    val facilityFetchedState = defaultModel.facilityFetched(facility)
    val hasPatientsState = facilityFetchedState.hasPatients(patientSearchResults.map(::PatientSearchResultUiState))

    with(testObserver) {
      assertNoErrors()
      assertValues(defaultModel, facilityFetchedState, hasPatientsState)
      assertNotTerminated()
    }
  }

  private fun screenCreated() {
    fixture.start()
  }
}
