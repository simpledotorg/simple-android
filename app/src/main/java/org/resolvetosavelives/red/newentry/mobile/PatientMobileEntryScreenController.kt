package org.resolvetosavelives.red.newentry.mobile

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

typealias Ui = PatientMobileEntryScreen
typealias UiChange = (Ui) -> Unit

class PatientMobileEntryScreenController @Inject constructor(
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
        .flatMap { Observable.just { ui: Ui -> ui.showKeyboardOnPrimaryMobileNumber() } }
  }

  private fun preFills(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .flatMapSingle { repository.ongoingEntry() }
        .filter { ongoingEntry -> ongoingEntry.mobileNumbers != null }
        .map { ongoingEntry -> ongoingEntry.mobileNumbers }
        .flatMap { mobileNumbers -> Observable.just { ui: Ui -> ui.preFill(mobileNumbers) } }
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val primaryNumberChanges = events
        .ofType<PatientPrimaryMobileTextChanged>()
        .map(PatientPrimaryMobileTextChanged::number)

    val secondaryNumberChanges = events
        .ofType<PatientSecondaryMobileTextChanged>()
        .map(PatientSecondaryMobileTextChanged::number)

    return events
        .ofType<PatientMobileEntryProceedClicked>()
        .flatMapSingle { repository.ongoingEntry() }
        .withLatestFrom(primaryNumberChanges, secondaryNumberChanges,
            { entry, primary, secondary -> entry to OngoingPatientEntry.MobileNumbers(primary, secondary) })
        .take(1)
        .map { (entry, updatedMobileNumbers) -> entry.copy(mobileNumbers = updatedMobileNumbers) }
        .flatMapCompletable { updatedEntry -> repository.save(updatedEntry) }
        .andThen(Observable.just { ui: Ui -> ui.openBloodPressureEntryScreen() })
  }
}
