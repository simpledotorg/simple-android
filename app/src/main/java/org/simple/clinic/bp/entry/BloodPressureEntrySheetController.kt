package org.simple.clinic.bp.entry

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_DIASTOLIC_EMPTY
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_DIASTOLIC_TOO_HIGH
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_DIASTOLIC_TOO_LOW
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_SYSTOLIC_EMPTY
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_SYSTOLIC_LESS_THAN_DIASTOLIC
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_SYSTOLIC_TOO_HIGH
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.ERROR_SYSTOLIC_TOO_LOW
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController.Validation.SUCCESS
import org.simple.clinic.bp.entry.OpenAs.NEW_BP
import org.simple.clinic.bp.entry.OpenAs.UPDATE_BP
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
        prefillWhenUpdatingABloodPressure(replayedEvents),
        bpValidationsAndSaves(replayedEvents)
    )
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

  private fun prefillWhenUpdatingABloodPressure(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<BloodPressureEntrySheetCreated>()
        .filter { it.openAs == UPDATE_BP }
        .flatMapSingle { bloodPressureRepository.findOne(it.uuid) }
        .map { bloodPressure -> { ui: Ui -> ui.updateBpMeasurements(bloodPressure.systolic, bloodPressure.diastolic) } }
  }

  private fun bpValidationsAndSaves(events: Observable<UiEvent>): Observable<UiChange> {
    val imeDoneClicks = events.ofType<BloodPressureSaveClicked>()

    val bloodPressureFromUpdateBp = events
        .ofType<BloodPressureEntrySheetCreated>()
        .filter { it.openAs == UPDATE_BP }
        .flatMapSingle { bloodPressureRepository.findOne(it.uuid) }
        .take(1)
        .cache()

    val patientUuidFromUpdateBp = bloodPressureFromUpdateBp
        .map { it.patientUuid }

    val patientUuidFromNewBp = events
        .ofType<BloodPressureEntrySheetCreated>()
        .filter { it.openAs == NEW_BP }
        .map { it.uuid }

    val patientUuids = Observable.merge(patientUuidFromNewBp, patientUuidFromUpdateBp)

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
              ERROR_SYSTOLIC_EMPTY -> ui.showSystolicEmptyError()
              ERROR_DIASTOLIC_EMPTY -> ui.showDiastolicEmptyError()
              SUCCESS -> {
                // Nothing to do here, SUCCESS handled below separately!
              }
            }.exhaustive()
          }
        }

    val openedAs = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.openAs }

    val saves = triples
        .filter { (_, systolic, diastolic) -> validateInput(systolic, diastolic) == SUCCESS }
        .distinctUntilChanged()
        .withLatestFrom(openedAs)
        .flatMap { (triple, openedAs) ->
          val (patientUuid, systolic, diastolic) = triple

          val saveBp: Completable = when (openedAs) {
            NEW_BP -> {
              bloodPressureRepository
                  .saveMeasurement(patientUuid, systolic.toInt(), diastolic.toInt())
                  .toCompletable()
            }
            UPDATE_BP -> {
              bloodPressureFromUpdateBp
                  .map { it.copy(systolic = systolic.toInt(), diastolic = diastolic.toInt()) }
                  .flatMapCompletable { bloodPressureMeasurement -> bloodPressureRepository.updateMeasurement(bloodPressureMeasurement) }
            }
            // Needed because of Java interop.
            else -> throw AssertionError("Opened as cannot be null!!")
          }

          saveBp.andThen(Observable.just({ ui: Ui -> ui.setBPSavedResultAndFinish() }))
        }

    return Observable.merge(errors, saves)
  }

  private fun validateInput(systolic: String, diastolic: String): Validation {
    if (systolic.isBlank()) {
      return ERROR_SYSTOLIC_EMPTY
    }
    if (diastolic.isBlank()) {
      return ERROR_DIASTOLIC_EMPTY
    }

    val systolicNumber = systolic.trim().toInt()
    val diastolicNumber = diastolic.trim().toInt()

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
    ERROR_SYSTOLIC_EMPTY,
    ERROR_DIASTOLIC_EMPTY,
    ERROR_SYSTOLIC_TOO_HIGH,
    ERROR_SYSTOLIC_TOO_LOW,
    ERROR_DIASTOLIC_TOO_HIGH,
    ERROR_DIASTOLIC_TOO_LOW,
    ERROR_SYSTOLIC_LESS_THAN_DIASTOLIC
  }
}
