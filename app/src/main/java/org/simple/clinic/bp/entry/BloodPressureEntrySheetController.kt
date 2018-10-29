package org.simple.clinic.bp.entry

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

    return Observable.mergeArray(
        automaticDiastolicFocusChanges(replayedEvents),
        validationErrorResets(replayedEvents),
        prefillWhenUpdatingABloodPressure(replayedEvents),
        bpValidations(replayedEvents),
        saveNewBp(replayedEvents),
        updateBp(replayedEvents))
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

  private fun bpValidations(events: Observable<UiEvent>): Observable<UiChange> {
    val imeDoneClicks = events.ofType<BloodPressureSaveClicked>()

    val systolicChanges = events
        .ofType<BloodPressureSystolicTextChanged>()
        .map { it.systolic }

    val diastolicChanges = events
        .ofType<BloodPressureDiastolicTextChanged>()
        .map { it.diastolic }

    return imeDoneClicks
        .withLatestFrom(systolicChanges, diastolicChanges) { _, systolic, diastolic -> systolic to diastolic }
        .map { (systolic, diastolic) ->
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
  }

  private fun saveNewBp(events: Observable<UiEvent>): Observable<UiChange> {
    val imeDoneClicks = events.ofType<BloodPressureSaveClicked>()

    val systolicChanges = events
        .ofType<BloodPressureSystolicTextChanged>()
        .map { it.systolic }

    val diastolicChanges = events
        .ofType<BloodPressureDiastolicTextChanged>()
        .map { it.diastolic }

    val validBpEntry = imeDoneClicks
        .withLatestFrom(systolicChanges, diastolicChanges) { _, systolic, diastolic -> systolic to diastolic }
        .map { (systolic, diastolic) -> Triple(systolic, diastolic, validateInput(systolic, diastolic)) }
        .filter { (_, _, validation) -> validation == SUCCESS }
        .map { (systolic, diastolic, _) -> systolic to diastolic }

    val patientUuid = events
        .ofType<BloodPressureEntrySheetCreated>()
        .filter { it.openAs == NEW_BP }
        .map { it.uuid }

    return imeDoneClicks
        .withLatestFrom(validBpEntry, patientUuid) { _, (systolic, diastolic), patientId -> Triple(patientId, systolic, diastolic) }
        .distinctUntilChanged()
        .flatMapSingle { (patientId, systolic, diastolic) ->
          bloodPressureRepository
              .saveMeasurement(
                  patientUuid = patientId,
                  systolic = systolic.toInt(),
                  diastolic = diastolic.toInt()
              )
        }
        .map { { ui: Ui -> ui.setBPSavedResultAndFinish() } }
  }

  private fun updateBp(events: Observable<UiEvent>): Observable<UiChange> {
    val imeDoneClicks = events.ofType<BloodPressureSaveClicked>()

    val systolicChanges = events
        .ofType<BloodPressureSystolicTextChanged>()
        .map { it.systolic }

    val diastolicChanges = events
        .ofType<BloodPressureDiastolicTextChanged>()
        .map { it.diastolic }

    val validBpEntry = imeDoneClicks
        .withLatestFrom(systolicChanges, diastolicChanges) { _, systolic, diastolic -> systolic to diastolic }
        .map { (systolic, diastolic) -> Triple(systolic, diastolic, validateInput(systolic, diastolic)) }
        .filter { (_, _, validation) -> validation == SUCCESS }
        .map { (systolic, diastolic, _) -> systolic to diastolic }

    val bloodPressure = events
        .ofType<BloodPressureEntrySheetCreated>()
        .filter { it.openAs == UPDATE_BP }
        .flatMapSingle { bloodPressureRepository.findOne(it.uuid) }
        .take(1)

    return imeDoneClicks
        .withLatestFrom(validBpEntry, bloodPressure) { _, (systolic, diastolic), savedBp -> Triple(savedBp, systolic, diastolic) }
        .distinctUntilChanged()
        .map { (savedBp, systolic, diastolic) -> savedBp.copy(systolic = systolic.toInt(), diastolic = diastolic.toInt()) }
        .flatMapSingle { bloodPressureMeasurement ->
          bloodPressureRepository.updateMeasurement(bloodPressureMeasurement)
              .toSingleDefault({ ui: Ui -> ui.setBPSavedResultAndFinish() })
        }
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
