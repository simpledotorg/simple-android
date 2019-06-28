package org.simple.clinic.allpatientsinfacility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class AllPatientsInFacilityUiStateProducer @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider
) : ObservableTransformer<UiEvent, AllPatientsInFacilityUiState> {
  override fun apply(
      uiEvents: Observable<UiEvent>
  ): ObservableSource<AllPatientsInFacilityUiState> {
    val initialState = AllPatientsInFacilityUiState.FETCHING_PATIENTS

    return uiEvents
        .ofType<ScreenCreated>()
        .flatMap {
          fetchAllPatientsInFacility(initialState)
              .startWith(initialState)
        }
  }

  private fun fetchAllPatientsInFacility(initialState: AllPatientsInFacilityUiState): Observable<AllPatientsInFacilityUiState> {
    return userSession
        .requireLoggedInUser()
        .subscribeOn(schedulersProvider.io())
        .flatMap { user ->
          val sharedFacilities = facilityRepository
              .currentFacility(user)
              .subscribeOn(schedulersProvider.io())
              .share()

          val facilityViewStates = sharedFacilities
              .map { initialState.facilityFetched(it) }

          val patientsViewStates = sharedFacilities
              .switchMap { patientRepository.allPatientsInFacility(it).subscribeOn(schedulersProvider.io()) }
              .withLatestFrom(facilityViewStates)
              .map { (searchResults, state) ->
                if (searchResults.isEmpty()) state.noPatients() else state.hasPatients(searchResults)
              }

          Observable.merge(facilityViewStates, patientsViewStates)
        }
  }
}
