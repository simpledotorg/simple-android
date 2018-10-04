package org.simple.clinic.bp.entry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_DIASTOLIC_TOO_HIGH
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_DIASTOLIC_TOO_LOW
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_SYSTOLIC_LESS_THAN_DIASTOLIC
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_SYSTOLIC_TOO_HIGH
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_SYSTOLIC_TOO_LOW
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.SUCCESS
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = BloodPressureEntrySheet
typealias UiChange = (Ui) -> Unit

class BloodPressureEntrySheetController @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.merge(
        automaticDiastolicFocusChanges(replayedEvents),
        validationErrorResets(replayedEvents),
        bpValidationsAndSaves(replayedEvents))
  }

  private fun automaticDiastolicFocusChanges(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<BloodPressureSystolicTextChanged>()
        .filter { shouldFocusDiastolic(it.systolic) }
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.changeFocusToDiastolic() } }
  }

  private fun shouldFocusDiastolic(systolicText: String): Boolean {
    return (systolicText.length == 3 && systolicText.matches("^[123].*$".toRegex()))
        || (systolicText.length == 2 && systolicText.matches("^[789].*$".toRegex()))
  }

  private fun validationErrorResets(events: Observable<UiEvent>): Observable<UiChange> {
    val systolicChanges = events.ofType<BloodPressureSystolicTextChanged>()
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.hideErrorMessage() } }

    val diastolicChanges = events.ofType<BloodPressureDiastolicTextChanged>()
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.hideErrorMessage() } }

    return Observable.merge(systolicChanges, diastolicChanges)
  }

  private fun bpValidationsAndSaves(events: Observable<UiEvent>): Observable<UiChange> {
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

    val triples = imeDoneClicks
        .withLatestFrom(patientUuids, systolicChanges, diastolicChanges) { _, uuid, systolic, diastolic -> Triple(uuid, systolic, diastolic) }

    val errors = triples
        .map { (_, systolic, diastolic) ->
          { ui: Ui ->
            when (validateInput(systolic, diastolic)) {
              ERROR_SYSTOLIC_LESS_THAN_DIASTOLIC -> ui.showSystolicLessThanDiastolicError()
              ERROR_SYSTOLIC_TOO_HIGH -> ui.showSystolicHighError()
              ERROR_SYSTOLIC_TOO_LOW -> ui.showSystolicLowError()
              ERROR_DIASTOLIC_TOO_HIGH -> ui.showDiastolicHighError()
              ERROR_DIASTOLIC_TOO_LOW -> ui.showDiastolicLowError()
              SUCCESS -> { // Nothing to do here, SUCCESS handled below separately!
              }
            }.exhaustive()
          }
        }

    val saves = triples
        .filter { (_, systolic, diastolic) -> validateInput(systolic, diastolic) == SUCCESS }
        .distinctUntilChanged()
        .flatMap { (uuid, systolic, diastolic) ->
          bloodPressureRepository
              .saveMeasurement(uuid, systolic.toInt(), diastolic.toInt())
              .toCompletable()
              .andThen(Observable.just({ ui: Ui -> ui.setBPSavedResultAndFinish() }))
        }

    return Observable.merge(errors, saves)
  }

  private fun validateInput(systolic: String, diastolic: String): Validation {
    val systolicNumber = systolic.toInt()
    val diastolicNumber = diastolic.toInt()

    return when {
      systolicNumber < 70 -> ERROR_SYSTOLIC_TOO_LOW
      systolicNumber > 300 -> ERROR_SYSTOLIC_TOO_HIGH
      diastolicNumber < 40 -> ERROR_DIASTOLIC_TOO_LOW
      diastolicNumber > 180 -> ERROR_DIASTOLIC_TOO_HIGH
      systolicNumber < diastolicNumber -> ERROR_SYSTOLIC_LESS_THAN_DIASTOLIC
      else -> SUCCESS
    }
  }

  enum class Validation {
    SUCCESS,
    ERROR_SYSTOLIC_TOO_HIGH,
    ERROR_SYSTOLIC_TOO_LOW,
    ERROR_DIASTOLIC_TOO_HIGH,
    ERROR_DIASTOLIC_TOO_LOW,
    ERROR_SYSTOLIC_LESS_THAN_DIASTOLIC
  }
}
