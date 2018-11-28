package org.simple.clinic.search.results

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.PersonalDetails
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

typealias Ui = PatientSearchResultsScreen
typealias UiChange = (Ui) -> Unit

class PatientSearchResultsController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val clock: Clock,
    @Named("short_date") private val dateOfBirthFormat: DateTimeFormatter
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events
        .compose(ReportAnalyticsEvents())
        .replay().refCount()

    return Observable.mergeArray(
        showComputedAge(replayedEvents),
        populateSearchResults(replayedEvents),
        openPatientSummary(replayedEvents),
        createNewPatient(replayedEvents))
  }

  private fun showComputedAge(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSearchResultsScreenCreated>()
        .map { it.key }
        .map { (_, age, dob) -> coerceAgeFrom(age, dob) }
        .map { { ui: Ui -> ui.showComputedAge(it.toString()) } }
  }

  private fun populateSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSearchResultsScreenCreated>()
        .map { it.key }
        .map { (name, age, dob) -> name to coerceAgeFrom(age, dob) }
        .flatMap { (name, computedAge) ->
          val facilities = userSession
              .requireLoggedInUser()
              .switchMap { facilityRepository.currentFacility(it) }

          val searchResults = patientRepository.search(name, computedAge, includeFuzzyNameSearch = true)

          Observables.combineLatest(searchResults, facilities)
              // We can't understand why, but search is occasionally
              // running on the main thread. This is a temporary solution.
              .subscribeOn(Schedulers.io())
        }
        .map { (results, currentFacility) ->
          { ui: Ui ->
            ui.updateSearchResults(results, currentFacility)
            ui.setEmptyStateVisible(results.isEmpty())
          }
        }
  }

  private fun coerceAgeFrom(age: String, dob: String): Int {
    return when {
      age.isNotBlank() -> age.trim().toInt()
      else -> {
        val dateOfBirth = dateOfBirthFormat.parse(dob.trim(), LocalDate::from)
        Period.between(dateOfBirth, LocalDate.now(clock)).years
      }
    }
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
              dateOfBirth = key.dateOfBirth,
              age = key.age,
              gender = null))
        }
        .flatMap {
          patientRepository
              .saveOngoingEntry(it)
              .andThen(Observable.just({ ui: Ui -> ui.openPatientEntryScreen() }))
        }
  }
}
