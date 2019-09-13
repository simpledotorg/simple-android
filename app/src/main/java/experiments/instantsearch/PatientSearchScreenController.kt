package experiments.instantsearch

import experiments.instantsearch.InstantPatientSearchExperimentsDao.PatientNamePhoneNumber
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.PatientToFacilityId
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.PatientStatus.Active
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

private typealias Ui = PatientSearchScreen
private typealias UiChange = (Ui) -> Unit

class PatientSearchScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val instantPatientSearchDao: InstantPatientSearchExperimentsDao,
    private val bloodPressureDao: BloodPressureMeasurement.RoomDao,
    private val locale: Locale
) : ObservableTransformer<UiEvent, UiChange> {

  private val whitespaceRegex = Regex("\\s")

  /**
   * Regular expression that matches digits with interleaved white spaces
   **/
  private val digitsRegex = Regex("[\\s*\\d+]+")

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    val patientDetails = instantPatientSearchDao.namePhoneNumber()
        .share()

    return Observable.mergeArray(
        openPatientSummary(replayedEvents),
        handleInstantSearch(replayedEvents, patientDetails)
    )
  }

  private fun handleInstantSearch(
      events: Observable<UiEvent>,
      patientDetails: Observable<List<PatientNamePhoneNumber>>
  ): ObservableSource<UiChange> {
    val textChanges = events
        .ofType<SearchQueryTextChanged>()
        .map { it.text }

    val hideSearchResults = textChanges
        .filter { it.isBlank() }
        .map { { ui: Ui -> ui.hideInstantSearchResults() } }

    val enteredTextChanges = textChanges.filter { !it.isBlank() }

    val facilityStream = userSession
        .loggedInUser()
        .filterAndUnwrapJust()
        .flatMap(facilityRepository::currentFacility)

    val showSearchResults = Observables.combineLatest(enteredTextChanges, patientDetails)
        .switchMap { (searchQuery, patientDetails) -> instantSearch(searchQuery, patientDetails) }
        .compose(PartitionSearchResultsByVisitedFacility(bloodPressureDao, facilityStream))
        .withLatestFrom(enteredTextChanges) { searchResults, enteredText -> searchResults.withSearchQuery(enteredText) }
        .map { searchResults -> { ui: Ui -> ui.showInstantSearchResults(searchResults) } }

    return Observable.merge(hideSearchResults, showSearchResults)
  }

  private fun instantSearch(
      searchQuery: String,
      patientDetails: List<PatientNamePhoneNumber>
  ): Observable<List<PatientSearchResult>> {
    val isPhoneNumberInput = Observable.just(searchQuery)
        .map { digitsRegex.matches(searchQuery) }

    val combinedSearchQuery = searchQuery.replace(whitespaceRegex, "")

    val searchByNumber = isPhoneNumberInput
        .filter { it }
        .map {
          patientDetails
              .filter { it.patientPhoneNumber != null }
              .filter { it.patientPhoneNumber!!.startsWith(searchQuery) }
              .map { it.patientUuid }
        }

    val searchByName = isPhoneNumberInput
        .filter { it.not() }
        .map {
          patientDetails
              .filter { patient ->
                val combinedName = patient.patientName.replace(whitespaceRegex, "")

                combinedName.startsWith(combinedSearchQuery, ignoreCase = true)
              }
              .map { it.patientUuid }
        }

    return Observable
        .merge(searchByNumber, searchByName)
        .switchMapSingle { uuids -> instantPatientSearchDao
            .searchByIds(uuids, Active)
            .subscribeOn(Schedulers.io())
            .map { results -> results.sortedBy { it.fullName.toLowerCase(locale) } }
        }
  }

  private fun openPatientSummary(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientItemClicked>()
        .map { it.patientUuid }
        .map { patientUuid -> { ui: Ui -> ui.openPatientSummary(patientUuid) } }
  }
}

class PartitionSearchResultsByVisitedFacility(
    private val bloodPressureDao: BloodPressureMeasurement.RoomDao,
    private val facilityStream: Observable<Facility>
) : ObservableTransformer<List<PatientSearchResult>, PatientSearchResults> {

  override fun apply(upstream: Observable<List<PatientSearchResult>>): ObservableSource<PatientSearchResults> {
    val searchResults = upstream.replay().refCount()

    val patientToFacilityUuidStream = searchResults
        .map { patients -> patients.map { it.uuid } }
        .switchMap {
          bloodPressureDao
              .patientToFacilityIds(it)
              .toObservable()
        }

    return Observables.combineLatest(searchResults, patientToFacilityUuidStream, facilityStream)
        .map { (patients, patientToFacilities, facility) ->
          val patientsToVisitedFacilities = mapPatientsToVisitedFacilities(patientToFacilities)

          val (patientsInCurrentFacility, patientsInOtherFacility) = patients.partition { patientSearchResult ->
            hasPatientVisitedFacility(
                patientsToVisitedFacilities = patientsToVisitedFacilities,
                facilityUuid = facility.uuid,
                patientUuid = patientSearchResult.uuid
            )
          }

          PatientSearchResults(
              visitedCurrentFacility = patientsInCurrentFacility,
              notVisitedCurrentFacility = patientsInOtherFacility,
              currentFacility = facility,
              searchQuery = ""
          )
        }
  }

  private fun hasPatientVisitedFacility(
      patientsToVisitedFacilities: Map<UUID, Set<UUID>>,
      facilityUuid: UUID,
      patientUuid: UUID
  ): Boolean {
    return patientsToVisitedFacilities[patientUuid]?.contains(facilityUuid) ?: false
  }

  private fun mapPatientsToVisitedFacilities(patientToFacilities: List<PatientToFacilityId>): Map<UUID, Set<UUID>> {
    return patientToFacilities
        .fold(mutableMapOf<UUID, MutableSet<UUID>>()) { facilityUuids, (patientUuid, facilityUuid) ->
          if (patientUuid !in facilityUuids) {
            facilityUuids[patientUuid] = mutableSetOf()
          }

          facilityUuids[patientUuid]?.add(facilityUuid)
          facilityUuids
        }
  }
}
