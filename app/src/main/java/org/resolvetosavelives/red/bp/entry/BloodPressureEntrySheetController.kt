package org.resolvetosavelives.red.bp.entry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.bp.BloodPressureRepository
import org.resolvetosavelives.red.facility.FacilityRepository
import org.resolvetosavelives.red.user.UserSession
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

typealias Ui = BloodPressureEntrySheetView
typealias UiChange = (Ui) -> Unit

class BloodPressureEntrySheetController @Inject constructor(val repository: BloodPressureRepository) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay(1).refCount()

    return Observable.mergeArray(handleImeOptionClicks(replayedEvents))
  }

  private fun handleImeOptionClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.patientUuid }

    val imeDoneClicks = events.ofType<BloodPressureSaveClicked>()

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
          repository
              .saveMeasurement(uuid, systolic.toInt(), diastolic.toInt())
              .andThen(Observable.just({ ui: Ui -> ui.dismiss() }))
        }
  }

  private fun isInputValid(systolic: String, diastolic: String): Boolean {
    return systolic.length in 2..3 && diastolic.length in 2..3
  }
}
