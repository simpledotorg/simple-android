package org.resolvetosavelives.red.newentry.bp

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PatientBpEntryScreen
private typealias UiChange = (Ui) -> Unit

class PatientBpEntryScreenController @Inject constructor(val repository: PatientRepository) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.merge(
        keyboardCalls(replayedEvents),
        preFills(replayedEvents),
        saveAndProceeds(replayedEvents))
  }

  private fun keyboardCalls(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType(ScreenCreated::class.java)
        .flatMap { Observable.just { ui: Ui -> ui.showKeyboardOnSystolicField() } }
  }

  private fun preFills(events: Observable<UiEvent>): Observable<UiChange> {
    val ongoingEntry = events.ofType(ScreenCreated::class.java)
        .flatMapSingle { repository.ongoingEntry() }

    val preFillFullName = ongoingEntry
        .flatMap { entry -> Observable.just { ui: Ui -> ui.preFill(entry.personalDetails!!.fullName) } }

    val preFillMeasurements = ongoingEntry
        .filter { entry -> entry.bloodPressureMeasurements != null }
        .flatMap { entry -> Observable.just { ui: Ui -> ui.preFill(entry.bloodPressureMeasurements!!) } }

    return preFillFullName.mergeWith(preFillMeasurements)
  }

  private fun saveAndProceeds(events: Observable<UiEvent>): Observable<UiChange> {
    val systolicChanges = events
        .ofType(PatientBpSystolicTextChanged::class.java)
        .map(PatientBpSystolicTextChanged::measurement)

    val diastolicChanges = events
        .ofType(PatientBpDiastolicTextChanged::class.java)
        .map(PatientBpDiastolicTextChanged::measurement)

    return events
        .ofType(PatientBpEntryProceedClicked::class.java)
        .flatMapSingle { repository.ongoingEntry() }
        .withLatestFrom(systolicChanges, diastolicChanges,
            { entry, systolic, diastolic -> entry to OngoingPatientEntry.BloodPressureMeasurement(systolic, diastolic) })
        .take(1)
        .map { (entry, updatedMeasurements) -> entry.copy(bloodPressureMeasurements = updatedMeasurements) }
        .flatMapCompletable { updatedEntry -> repository.saveOngoingEntry(updatedEntry) }
        .andThen(Observable.just({ ui: Ui -> ui.openDrugSelectionScreen() }))
  }
}
