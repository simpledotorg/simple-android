package org.resolvetosavelives.red.newentry.search

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientSearchByMobileScreen
private typealias UiChange = (Ui) -> Unit

class PatientSearchByMobileScreenController @Inject constructor(
    private val repository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        screenSetup(),
        patientSearchResults(replayedEvents),
        saveAndProceeds(replayedEvents))
  }

  private fun screenSetup(): Observable<UiChange> {
    return Observable.just({ ui: Ui -> ui.showKeyboardOnMobileNumberField() }, { ui: Ui -> ui.setupSearchResultsList() })
  }

  private fun patientSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType(PatientMobileNumberTextChanged::class.java)
        .map(PatientMobileNumberTextChanged::number)
        .flatMap(repository::search)
        .map { matchingPatients -> { ui: Ui -> ui.updatePatientSearchResults(matchingPatients) } }
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val mobileNumberChanges = events
        .ofType(PatientMobileNumberTextChanged::class.java)
        .map(PatientMobileNumberTextChanged::number)

    return events
        .ofType(PatientSearchByMobileProceedClicked::class.java)
        .withLatestFrom(mobileNumberChanges, { _, number -> number })
        .take(1)
        .map { number -> OngoingPatientEntry(mobileNumber = number) }
        .flatMapCompletable { newEntry -> repository.save(newEntry) }
        .andThen(Observable.just { ui: Ui -> ui.openPersonalDetailsEntryScreen() })
  }
}
