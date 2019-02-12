package org.simple.clinic.bp.entry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.ReplayUntilScreenIsDestroyed
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
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import javax.inject.Inject

@Deprecated(message = "Use BloodPressureEntrySheetControllerV2 instead")
class BloodPressureEntrySheetController @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val utcClock: UtcClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        automaticFocusChanges(replayedEvents),
        validationErrorResets(replayedEvents),
        prefillWhenUpdatingABloodPressure(replayedEvents),
        bpValidations(replayedEvents),
        saveNewBp(replayedEvents),
        updateBp(replayedEvents),
        toggleRemoveBloodPressureButton(replayedEvents),
        updateSheetTitle(replayedEvents),
        showConfirmRemoveBloodPressureDialog(replayedEvents),
        closeSheetWhenEditedBpIsDeleted(replayedEvents))
  }

  private fun automaticFocusChanges(events: Observable<UiEvent>): Observable<UiChange> {
    val isSystolicValueComplete = { systolicText: String ->
      (systolicText.length == 3 && systolicText.matches("^[123].*$".toRegex()))
          || (systolicText.length == 2 && systolicText.matches("^[789].*$".toRegex()))
    }

    val moveFocusToDiastolic = events.ofType<BloodPressureSystolicTextChanged>()
        .filter { isSystolicValueComplete(it.systolic) }
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.changeFocusToDiastolic() } }

    data class BloodPressure(val systolic: String, val diastolic: String)

    val systolicChanges = events.ofType<BloodPressureSystolicTextChanged>().map { it.systolic }
    val diastolicChanges = events.ofType<BloodPressureDiastolicTextChanged>().map { it.diastolic }

    val bpChanges = Observables
        .combineLatest(systolicChanges, diastolicChanges)
        .map { (systolic, diastolic) -> BloodPressure(systolic, diastolic) }

    val diastolicBackspaceClicksWithEmptyText = events.ofType<BloodPressureDiastolicBackspaceClicked>()
        .withLatestFrom(bpChanges) { _, bp -> bp }
        .filter { bp -> bp.diastolic.isEmpty() }

    val moveFocusBackToSystolic = diastolicBackspaceClicksWithEmptyText
        .map { { ui: Ui -> ui.changeFocusToSystolic() } }

    val deleteLastDigitOfSystolic = diastolicBackspaceClicksWithEmptyText
        .filter { bp -> bp.systolic.isNotBlank() }
        .map { bp -> bp.systolic.substring(0, bp.systolic.length - 1) }
        .map { truncatedSystolic -> { ui: Ui -> ui.setSystolic(truncatedSystolic) } }

    return moveFocusToDiastolic
        .mergeWith(moveFocusBackToSystolic)
        .mergeWith(deleteLastDigitOfSystolic)
  }

  private fun validationErrorResets(events: Observable<UiEvent>): Observable<UiChange> {
    val systolicChanges = events.ofType<BloodPressureSystolicTextChanged>()
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.hideBpErrorMessage() } }

    val diastolicChanges = events.ofType<BloodPressureDiastolicTextChanged>()
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.hideBpErrorMessage() } }

    return Observable.merge(systolicChanges, diastolicChanges)
  }

  private fun prefillWhenUpdatingABloodPressure(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<BloodPressureEntrySheetCreated>()
        .filter { it.openAs is OpenAs.Update }
        .map { it.openAs as OpenAs.Update }
        .flatMap {
          // Subscribing on the IO scheduler should not be necessary here, but we've seen
          // occasional crashes because it sometimes runs on the main thread. This is a temp.
          // workaround until we figure out what's actually happening.
          bloodPressureRepository
              .measurement(it.bpUuid)
              .subscribeOn(io())
              .take(1L)
        }
        .map { bloodPressure ->
          { ui: Ui ->
            ui.setSystolic(bloodPressure.systolic.toString())
            ui.setDiastolic(bloodPressure.diastolic.toString())
          }
        }
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
        .filter { it.openAs is OpenAs.New }
        .map { (it.openAs as OpenAs.New).patientUuid }

    return imeDoneClicks
        .withLatestFrom(validBpEntry, patientUuid) { _, (systolic, diastolic), patientId -> Triple(patientId, systolic, diastolic) }
        .distinctUntilChanged()
        .flatMapSingle { (patientId, systolic, diastolic) ->
          bloodPressureRepository
              .saveMeasurement(
                  patientUuid = patientId,
                  systolic = systolic.toInt(),
                  diastolic = diastolic.toInt(),
                  createdAt = Instant.now(utcClock)
              )
        }
        .map { { ui: Ui -> ui.setBpSavedResultAndFinish() } }
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
        .map { it.openAs }
        .ofType<OpenAs.Update>()
        .flatMap { bloodPressureRepository.measurement(it.bpUuid) }
        .take(1)

    return imeDoneClicks
        .withLatestFrom(validBpEntry, bloodPressure) { _, (systolic, diastolic), savedBp -> Triple(savedBp, systolic, diastolic) }
        .distinctUntilChanged()
        .map { (savedBp, systolic, diastolic) -> savedBp.copy(systolic = systolic.toInt(), diastolic = diastolic.toInt()) }
        .flatMapSingle { bloodPressureMeasurement ->
          bloodPressureRepository.updateMeasurement(bloodPressureMeasurement)
              .toSingleDefault({ ui: Ui -> ui.setBpSavedResultAndFinish() })
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

  private fun toggleRemoveBloodPressureButton(events: Observable<UiEvent>): Observable<UiChange> {
    val hideRemoveBpButton = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.openAs }
        .ofType<OpenAs.New>()
        .map { { ui: Ui -> ui.hideRemoveBpButton() } }

    val showRemoveBpButton = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.openAs }
        .ofType<OpenAs.Update>()
        .map { { ui: Ui -> ui.showRemoveBpButton() } }

    return hideRemoveBpButton.mergeWith(showRemoveBpButton)
  }

  private fun updateSheetTitle(events: Observable<UiEvent>): Observable<out UiChange>? {
    val openAsStream = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.openAs }

    val showEnterBloodPressureTitle = openAsStream
        .filter { it is OpenAs.New }
        .map { { ui: Ui -> ui.showEnterNewBloodPressureTitle() } }

    val showEditBloodPressureTitle = openAsStream
        .filter { it is OpenAs.Update }
        .map { { ui: Ui -> ui.showEditBloodPressureTitle() } }

    return showEnterBloodPressureTitle.mergeWith(showEditBloodPressureTitle)
  }

  private fun showConfirmRemoveBloodPressureDialog(events: Observable<UiEvent>): Observable<out UiChange>? {
    val bloodPressureMeasurementUuidStream = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.openAs }
        .ofType<OpenAs.Update>()
        .map { it.bpUuid }

    val removeClicks = events.ofType<BloodPressureRemoveClicked>()

    return removeClicks
        .withLatestFrom(bloodPressureMeasurementUuidStream)
        .map { (_, uuid) -> { ui: Ui -> ui.showConfirmRemoveBloodPressureDialog(uuid) } }
  }

  private fun closeSheetWhenEditedBpIsDeleted(events: Observable<UiEvent>): Observable<UiChange>? {
    val bloodPressureMeasurementUuidStream = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.openAs }
        .ofType<OpenAs.Update>()
        .map { it.bpUuid }

    return bloodPressureMeasurementUuidStream
        .flatMap(bloodPressureRepository::measurement)
        .filter { it.deletedAt != null }
        .take(1)
        .map { { ui: Ui -> ui.setBpSavedResultAndFinish() } }
  }
}
