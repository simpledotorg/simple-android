package org.simple.clinic.shortcodesearchresult

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.plumbing.BaseUiStateProducer
import org.simple.clinic.searchresultsview.PartitionSearchResultsByVisitedFacility
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

class ShortCodeSearchResultStateProducer(
    private val shortCode: String,
    private val patientRepository: PatientRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val bloodPressureDao: BloodPressureMeasurement.RoomDao,
    private val ui: ShortCodeSearchResultUi,
    private val schedulersProvider: SchedulersProvider,
    private val initialState: ShortCodeSearchResultState,
    private val currentStateProvider: () -> ShortCodeSearchResultState
) : BaseUiStateProducer<UiEvent, ShortCodeSearchResultState>() {

  private val currentState: ShortCodeSearchResultState
    get() = currentStateProvider()

  override fun apply(events: Observable<UiEvent>): ObservableSource<ShortCodeSearchResultState> {
    return Observable.never()
  }

  private fun initialStates(events: Observable<UiEvent>) = events
      .ofType<ScreenCreated>()
      .map { initialState }

  private fun fetchPatients(events: Observable<UiEvent>): Observable<ShortCodeSearchResultState> {
    val shortCodeSearchResults = events.ofType<ScreenCreated>()
        .flatMap {
          patientRepository
              .searchByShortCode(initialState.shortCode)
              .subscribeOn(schedulersProvider.io())
        }
        .share()

    val noMatchingPatient = shortCodeSearchResults
        .filter { it.isEmpty() }
        .map { currentState.noMatchingPatients() }

    val currentFacilityStream = facilityRepository.currentFacility()

    val matchingPatients = shortCodeSearchResults
        .filter { it.isNotEmpty() }
        .compose(PartitionSearchResultsByVisitedFacility(bloodPressureDao, currentFacilityStream))
        .map(currentState::patientsFetched)

    return matchingPatients.mergeWith(noMatchingPatient)
  }
}
