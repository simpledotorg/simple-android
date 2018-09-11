package org.simple.clinic.search

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientSearchScreen
private typealias UiChange = (Ui) -> Unit

class PatientSearchScreenController @Inject constructor(
    private val repository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  // TODO: This is obviously a bad idea. Fix this.
  private var ageText = ""

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        showCreatePatientButton(replayedEvents),
        enableSearchButton(replayedEvents),
        searchResults(replayedEvents),
        openAgeFilterSheet(replayedEvents),
        openPatientSummary(replayedEvents),
        saveAndProceeds(replayedEvents))
  }

  private fun showCreatePatientButton(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<SearchQueryNameChanged>()
        .map {
          when {
            it.name.isNotBlank() -> { ui: Ui -> ui.showCreatePatientButton(true) }
            else -> { ui: Ui -> ui.showCreatePatientButton(false) }
          }
        }
  }

  private fun enableSearchButton(events: Observable<UiEvent>): Observable<UiChange> {
    val nameChanges = events
        .ofType<SearchQueryNameChanged>()
        .map { it.name }

    val ageChanges = events
        .ofType<SearchQueryAgeChanged>()
        .map { it.ageString }

    return Observables.combineLatest(nameChanges, ageChanges)
        .map { (name, age) -> name.isNotBlank() && age.isNotBlank() }
        .map { isQueryComplete ->
          { ui: Ui ->
            if (isQueryComplete) {
              ui.showSearchButtonAsEnabled()
            } else {
              ui.showSearchButtonAsDisabled()
            }
          }
        }
  }

  private fun searchResults(events: Observable<UiEvent>): Observable<UiChange> {
    val nameChanges = events
        .ofType<SearchQueryNameChanged>()
        .map { it.name.trim() }

    val ageChanges = events
        .ofType<SearchQueryAgeChanged>()
        .map { it.ageString }
        .doOnNext { ageText = it }  // this will go away soon when age bottom sheet is removed.

    return events
        .ofType<SearchClicked>()
        .withLatestFrom(nameChanges, ageChanges)
        .filter { (_, name, age) -> name.isNotBlank() && age.isNotBlank() }
        .switchMap { (_, name, age) -> repository.search(name, age.toInt()) }
        .map { matchingPatients -> { ui: Ui -> ui.updatePatientSearchResults(matchingPatients) } }
  }

  private fun openPatientSummary(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SearchResultClicked>()
        .map { it.searchResult }
        .map { clickedPatient -> { ui: Ui -> ui.openPatientSummaryScreen(clickedPatient.uuid) } }
  }

  private fun openAgeFilterSheet(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SearchQueryAgeFilterClicked>()
        .map { { ui: Ui -> ui.openAgeFilterSheet(ageText) } }
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val nameChanges = events
        .ofType<SearchQueryNameChanged>()
        .map { it.name.trim() }

    return events
        .ofType<CreateNewPatientClicked>()
        .withLatestFrom(nameChanges) { _, name -> name }
        .take(1)
        .map { OngoingPatientEntry(personalDetails = OngoingPatientEntry.PersonalDetails(it, null, null, null)) }
        .flatMapCompletable { newEntry -> repository.saveOngoingEntry(newEntry) }
        .andThen(Observable.just { ui: Ui -> ui.openPersonalDetailsEntryScreen() })
  }
}
