package org.simple.clinic.search.results

import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.PersonalDetails
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchResults
import org.simple.clinic.search.results.PatientSearchResultsItemType.InCurrentFacilityHeader
import org.simple.clinic.search.results.PatientSearchResultsItemType.NoPatientsInCurrentFacility
import org.simple.clinic.search.results.PatientSearchResultsItemType.NotInCurrentFacilityHeader
import org.simple.clinic.search.results.PatientSearchResultsItemType.PatientSearchResultRow
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

typealias Ui = PatientSearchResultsScreen
typealias UiChange = (Ui) -> Unit

class PatientSearchResultsController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val phoneObfuscator: PhoneNumberObfuscator,
    private val utcClock: UtcClock,
    val userClock: UserClock,
    @Named("date_for_search_results") private val dateOfBirthFormatter: DateTimeFormatter
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events
        .compose(ReportAnalyticsEvents())
        .replay().refCount()

    return Observable.mergeArray(
        populateSearchResults(replayedEvents),
        openPatientSummary(replayedEvents),
        createNewPatient(replayedEvents))
  }

  private fun populateSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSearchResultsScreenCreated>()
        .map { it.key }
        .flatMap { (name) ->
          val facilities = userSession
              .requireLoggedInUser()
              .switchMap { facilityRepository.currentFacility(it) }

          val searchResults = patientRepository.search(name)

          Observables.combineLatest(searchResults, facilities)
              // We can't understand why, but search is occasionally
              // running on the main thread. This is a temporary solution.
              .subscribeOn(Schedulers.io())
        }
        .map { (results, currentFacility) ->
          { ui: Ui ->
            ui.updateSearchResults(generateListItems(results, currentFacility))
            ui.setEmptyStateVisible(results.visitedCurrentFacility.isEmpty() && results.notVisitedCurrentFacility.isEmpty())
          }
        }
  }

  private fun generateListItems(
      results: PatientSearchResults,
      currentFacility: Facility
  ): List<PatientSearchResultsItemType<out ViewHolder>> {
    if (results.visitedCurrentFacility.isEmpty() && results.notVisitedCurrentFacility.isEmpty()) return emptyList()

    val itemsInCurrentFacility = if (results.visitedCurrentFacility.isNotEmpty()) {
      results.visitedCurrentFacility.map {
        PatientSearchResultRow(it, currentFacility, phoneObfuscator, dateOfBirthFormatter, utcClock, userClock)
      }
    } else {
      listOf(NoPatientsInCurrentFacility)
    }

    val itemsInOtherFacility = if (results.notVisitedCurrentFacility.isNotEmpty()) {
      listOf(NotInCurrentFacilityHeader) +
          results.notVisitedCurrentFacility.map {
            PatientSearchResultRow(it, currentFacility, phoneObfuscator, dateOfBirthFormatter, utcClock, userClock)
          }
    } else {
      emptyList()
    }
    return listOf(InCurrentFacilityHeader(facilityName = currentFacility.name)) +
        itemsInCurrentFacility +
        itemsInOtherFacility
  }

  private fun openPatientSummary(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSearchResultClicked>()
        .map { it.searchResult.uuid }
        .map { { ui: Ui -> ui.openPatientSummaryScreen(patientUuid = it) } }
  }

  private fun createNewPatient(events: Observable<UiEvent>): Observable<UiChange> {
    val screenKey = events
        .ofType<PatientSearchResultsScreenCreated>()
        .map { it.key }

    return events.ofType<CreateNewPatientClicked>()
        .withLatestFrom(screenKey)
        .map { (_, key) ->
          OngoingNewPatientEntry(PersonalDetails(
              fullName = key.fullName,
              dateOfBirth = null,
              age = null,
              gender = null))
        }
        .flatMap {
          patientRepository
              .saveOngoingEntry(it)
              .andThen(Observable.just({ ui: Ui -> ui.openPatientEntryScreen() }))
        }
  }
}
