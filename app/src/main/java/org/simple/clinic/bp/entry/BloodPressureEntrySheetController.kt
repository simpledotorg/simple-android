package org.simple.clinic.bp.entry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = BloodPressureEntrySheet
typealias UiChange = (Ui) -> Unit

class BloodPressureEntrySheetController @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay(1).refCount()

    return Observable.merge(
        handleImeOptionClicks(replayedEvents),
        systolicTextChanges(replayedEvents))
  }

  private fun systolicTextChanges(events: Observable<UiEvent>): Observable<UiChange> {
    // TODO: This needs unit tests
    return events
        .ofType<BloodPressureSystolicTextChanged>()
        .distinctUntilChanged()
        .filter { shouldFocusDiastolic(it.systolic) }
        .map { { ui: Ui -> ui.changeFocusToDiastolic() } }
  }

  private fun shouldFocusDiastolic(systolicText: String): Boolean {
    return (systolicText.length == 3 && systolicText.matches("^[12].*$".toRegex()))
        || (systolicText.length == 2 && systolicText.matches("^[3-9].*$".toRegex()))
  }

  private fun handleImeOptionClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val imeDoneClicks = events.ofType<BloodPressureSaveClicked>()

    val patientUuids = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.patientUuid }

    val systolicChanges = events
        .ofType<BloodPressureSystolicTextChanged>()
        .map { it.systolic }

    val diastolicChanges = events
        .ofType<BloodPressureDiastolicTextChanged>()
        .map { it.diastolic }

    return imeDoneClicks
        .withLatestFrom(patientUuids, systolicChanges, diastolicChanges) { _, uuid, systolic, diastolic -> Triple(uuid, systolic, diastolic) }
        .filter { (_, systolic, diastolic) -> isInputValid(systolic, diastolic) }
        .take(1)
        .flatMap { (uuid, systolic, diastolic) ->
          bloodPressureRepository
              .saveMeasurement(uuid, systolic.toInt(), diastolic.toInt())
              .toCompletable()
              .andThen(Observable.just({ ui: Ui -> ui.setBPSavedResultAndFinish() }))
        }
  }

  private fun isInputValid(systolic: String, diastolic: String): Boolean {
    return systolic.length in 2..3 && diastolic.length in 2..3
  }
}
