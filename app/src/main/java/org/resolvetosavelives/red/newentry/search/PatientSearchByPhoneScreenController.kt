package org.resolvetosavelives.red.newentry.search

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientSearchByPhoneScreen
private typealias UiChange = (Ui) -> Unit

class PatientSearchByPhoneScreenController @Inject constructor(
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
    return Observable.just({ ui: Ui -> ui.showKeyboardOnPhoneNumberField() }, { ui: Ui -> ui.setupSearchResultsList() })
  }

  private fun patientSearchResults(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType(PatientPhoneNumberTextChanged::class.java)
        .map(PatientPhoneNumberTextChanged::number)
        .flatMap(repository::search)
        .map { matchingPatients -> { ui: Ui -> ui.updatePatientSearchResults(matchingPatients) } }
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberChanges = events
        .ofType(PatientPhoneNumberTextChanged::class.java)
        .map(PatientPhoneNumberTextChanged::number)

    return events
        .ofType(PatientSearchByPhoneProceedClicked::class.java)
        .withLatestFrom(phoneNumberChanges, { _, number -> number })
        .take(1)
        .map { number -> OngoingPatientEntry(phoneNumbers = OngoingPatientEntry.PhoneNumbers(primary = number)) }
        .flatMapCompletable { newEntry -> repository.save(newEntry) }
        .andThen(Observable.just { ui: Ui -> ui.openPersonalDetailsEntryScreen() })
  }
}
