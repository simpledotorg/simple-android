package org.resolvetosavelives.red.search

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.patient.OngoingPatientEntry
import org.resolvetosavelives.red.patient.PatientRepository
import org.resolvetosavelives.red.widgets.UiEvent
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
        patientSearchResults(replayedEvents),
        saveAndProceeds(replayedEvents),
        backButtonClicks(replayedEvents),
        searchResultClicks(replayedEvents))
  }

  private fun screenSetup(): Observable<UiChange> {
    return Observable.just({ ui: Ui -> ui.showKeyboardOnPhoneNumberField() }, { ui: Ui -> ui.setupSearchResultsList() })
  }

  private fun patientSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SearchQueryTextChanged>()
        .map(SearchQueryTextChanged::query)
        .switchMap { repository.searchPatientsAndPhoneNumbers(it) }
        .map { matchingPatients -> { ui: Ui -> ui.updatePatientSearchResults(matchingPatients) } }
  }

  private fun searchResultClicks(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientSearchResultClicked>()
        .map { it.searchResult }
        .map { clickedPatient -> { ui: Ui -> ui.openPatientSummaryScreen(clickedPatient.uuid) } }
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberChanges = events
        .ofType<SearchQueryTextChanged>()
        .map(SearchQueryTextChanged::query)

    return events
        .ofType<CreateNewPatientClicked>()
        .withLatestFrom(phoneNumberChanges, { _, number -> number })
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
