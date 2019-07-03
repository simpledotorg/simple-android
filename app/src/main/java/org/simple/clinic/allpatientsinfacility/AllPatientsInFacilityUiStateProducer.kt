package org.simple.clinic.allpatientsinfacility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.plumbing.BaseUiStateProducer
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenRestored
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class AllPatientsInFacilityUiStateProducer @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider
) : BaseUiStateProducer<UiEvent, AllPatientsInFacilityUiState>() {

  override fun apply(uiEvents: Observable<UiEvent>): ObservableSource<AllPatientsInFacilityUiState> {
    val initialState = AllPatientsInFacilityUiState.FETCHING_PATIENTS

    return Observable.merge(
        screenCreatedUseCase(initialState, uiEvents.ofType(ScreenCreated::class.java)),
        screenRestoredUseCase(uiEvents.ofType(ScreenRestored::class.java))
    )
  }

  private fun screenCreatedUseCase(
      initialState: AllPatientsInFacilityUiState,
      screenCreatedEvents: Observable<ScreenCreated>
  ): Observable<AllPatientsInFacilityUiState> {
    return screenCreatedEvents
        .flatMap {
          fetchAllPatientsInFacility()
              .startWith(initialState)
        }
  }

  private fun fetchAllPatientsInFacility(): Observable<AllPatientsInFacilityUiState> {
    return userSession
        .requireLoggedInUser()
        .subscribeOn(schedulersProvider.io())
        .switchMap { user ->
          val sharedFacilities = facilityRepository
              .currentFacility(user)
              .subscribeOn(schedulersProvider.io())
              .share()

          val facilityViewStates = sharedFacilities
              .withLatestFrom(states) { facility, state -> state.facilityFetched(facility) }

          val patientsViewStates = sharedFacilities
              .switchMap { patientRepository.allPatientsInFacility(it).subscribeOn(schedulersProvider.io()) }
              .withLatestFrom(facilityViewStates)
              .map { (searchResults, state) ->
                if (searchResults.isEmpty()) state.noPatients() else state.hasPatients(searchResults)
              }

          Observable.merge(facilityViewStates, patientsViewStates)
        }
  }

  private fun screenRestoredUseCase(
      screenRestoredEvents: Observable<ScreenRestored>
  ): Observable<AllPatientsInFacilityUiState> {
    return screenRestoredEvents
        .ofType<ScreenRestored>()
        .withLatestFrom(states) { _, state -> state }
  }
}
