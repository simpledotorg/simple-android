package org.simple.clinic.search

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientSearchScreen
private typealias UiChange = (Ui) -> Unit

class PatientSearchScreenController @Inject constructor(
    private val repository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay(1).refCount()

    return Observable.mergeArray(
        screenSetup(),
        searchResults(replayedEvents),
        ageFilterClicks(replayedEvents),
        searchResultClicks(replayedEvents),
        saveAndProceeds(replayedEvents),
        backButtonClicks(replayedEvents))
  }

  private fun screenSetup(): Observable<UiChange> {
    return Observable.just({ ui: Ui -> ui.showKeyboardOnSearchEditText() }, { ui: Ui -> ui.setupSearchResultsList() })
  }

  private fun searchResults(events: Observable<UiEvent>): Observable<UiChange> {
    val queryChanges = events
        .ofType<SearchQueryTextChanged>()
        .map { it.query }

    val ageChanges = events
        .ofType<SearchQueryAgeChanged>()
        .map { it.ageString }

    return Observables.combineLatest(queryChanges, ageChanges)
        .switchMap { (query, age) ->
          when {
            age.isEmpty() -> repository.searchPatientsAndPhoneNumbers(query)
            else -> repository.searchPatientsAndPhoneNumbers(query, age.toInt())
          }
        }
        .map { matchingPatients -> { ui: Ui -> ui.updatePatientSearchResults(matchingPatients) } }
  }

  private fun searchResultClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SearchResultClicked>()
        .map { it.searchResult }
        .map { clickedPatient -> { ui: Ui -> ui.openPatientSummaryScreen(clickedPatient.uuid) } }
  }

  private fun ageFilterClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SearchQueryAgeFilterClicked>()
        .map { { ui: Ui -> ui.openAgeFilterSheet() } }
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberChanges = events
        .ofType<SearchQueryTextChanged>()
        .map(SearchQueryTextChanged::query)

    return events
        .ofType<CreateNewPatientClicked>()
        .withLatestFrom(phoneNumberChanges) { _, number -> number }
        .take(1)
        .map { number -> OngoingPatientEntry(phoneNumber = OngoingPatientEntry.PhoneNumber(number)) }
        .flatMapCompletable { newEntry -> repository.saveOngoingEntry(newEntry) }
        .andThen(Observable.just { ui: Ui -> ui.openPersonalDetailsEntryScreen() })
  }

  private fun backButtonClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<BackButtonClicked>()
        .map { { ui: Ui -> ui.goBackToHomeScreen() } }
  }
}
