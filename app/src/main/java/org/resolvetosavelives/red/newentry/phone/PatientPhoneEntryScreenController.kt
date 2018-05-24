package org.resolvetosavelives.red.newentry.phone

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientPhoneEntryScreen
private typealias UiChange = (Ui) -> Unit

class PatientPhoneEntryScreenController @Inject constructor(
    private val repository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        keyboardCalls(replayedEvents),
        preFills(replayedEvents),
        saveAndProceeds(replayedEvents))
  }

  private fun keyboardCalls(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .flatMap { Observable.just { ui: Ui -> ui.showKeyboardOnPrimaryPhoneNumber() } }
  }

  private fun preFills(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .flatMapSingle { repository.ongoingEntry() }
        .filter { ongoingEntry -> ongoingEntry.phoneNumbers != null }
        .map { ongoingEntry -> ongoingEntry.phoneNumbers }
        .flatMap { phoneNumbers -> Observable.just { ui: Ui -> ui.preFill(phoneNumbers) } }
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val primaryNumberChanges = events
        .ofType<PatientPrimaryPhoneTextChanged>()
        .map(PatientPrimaryPhoneTextChanged::number)

    val secondaryNumberChanges = events
        .ofType<PatientSecondaryPhoneTextChanged>()
        .map(PatientSecondaryPhoneTextChanged::number)

    return events
        .ofType<PatientPhoneEntryProceedClicked>()
        .flatMapSingle { repository.ongoingEntry() }
        .withLatestFrom(primaryNumberChanges, secondaryNumberChanges,
            { entry, primary, secondary -> entry to OngoingPatientEntry.PhoneNumbers(primary, secondary) })
        .take(1)
        .map { (entry, updatedPhoneNumbers) -> entry.copy(phoneNumbers = updatedPhoneNumbers) }
        .flatMapCompletable { updatedEntry -> repository.saveOngoingEntry(updatedEntry) }
        .andThen(Observable.just { ui: Ui -> ui.openBloodPressureEntryScreen() })
  }
}
