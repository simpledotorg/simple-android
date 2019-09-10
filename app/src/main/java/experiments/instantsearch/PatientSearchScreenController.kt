package experiments.instantsearch

import experiments.instantsearch.InstantPatientSearchExperimentsDao.PatientNamePhoneNumber
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientStatus.Active
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientSearchScreen
private typealias UiChange = (Ui) -> Unit

class PatientSearchScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val instantPatientSearchDao: InstantPatientSearchExperimentsDao
) : ObservableTransformer<UiEvent, UiChange> {

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

    val showSearchResults = Observables.combineLatest(enteredTextChanges, patientDetails)
        .switchMap { (searchQuery, patientDetails) -> instantSearch(searchQuery, patientDetails) }

    return Observable.merge(hideSearchResults, showSearchResults)
  }

  private fun instantSearch(
      searchQuery: String,
      patientDetails: List<PatientNamePhoneNumber>
  ): Observable<UiChange> {
    val isPhoneNumberInput = Observable.just(searchQuery)
        .map { digitsRegex.matches(searchQuery) }

    val searchByNumber = isPhoneNumberInput
        .filter { it }
        .map {
          patientDetails
              .filter { it.patientPhoneNumber != null }
              .filter { it.patientPhoneNumber!!.contains(searchQuery) }
              .map { it.patientUuid }
        }

    val searchByName = isPhoneNumberInput
        .filter { it.not() }
        .map {
          patientDetails
              .filter { it.patientName.contains(searchQuery, ignoreCase = true) }
              .map { it.patientUuid }
        }

    val searchResults = Observable
        .merge(searchByNumber, searchByName)
        .switchMapSingle { uuids -> instantPatientSearchDao.searchByIds(uuids, Active) }

    return Observable.empty()
  }

  private fun openPatientSummary(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientItemClicked>()
        .map { it.patientUuid }
        .map { patientUuid -> { ui: Ui -> ui.openPatientSummary(patientUuid) } }
  }
}
