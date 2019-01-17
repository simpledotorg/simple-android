package org.simple.clinic.bp.entry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureConfig
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.DATE_ENTRY
import org.simple.clinic.bp.entry.BloodPressureEntrySheetControllerV2.Validation.ErrorDiastolicEmpty
import org.simple.clinic.bp.entry.BloodPressureEntrySheetControllerV2.Validation.ErrorDiastolicTooHigh
import org.simple.clinic.bp.entry.BloodPressureEntrySheetControllerV2.Validation.ErrorDiastolicTooLow
import org.simple.clinic.bp.entry.BloodPressureEntrySheetControllerV2.Validation.ErrorSystolicEmpty
import org.simple.clinic.bp.entry.BloodPressureEntrySheetControllerV2.Validation.ErrorSystolicLessThanDiastolic
import org.simple.clinic.bp.entry.BloodPressureEntrySheetControllerV2.Validation.ErrorSystolicTooHigh
import org.simple.clinic.bp.entry.BloodPressureEntrySheetControllerV2.Validation.ErrorSystolicTooLow
import org.simple.clinic.bp.entry.BloodPressureEntrySheetControllerV2.Validation.Success
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator.Result2.Invalid
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator.Result2.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator.Result2.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthFormatValidator.Result2.Valid
import org.threeten.bp.ZoneOffset.UTC
import javax.inject.Inject

typealias Ui = BloodPressureEntrySheet
typealias UiChange = (Ui) -> Unit

/**
 * V2: Includes date entry.
 */
class BloodPressureEntrySheetControllerV2 @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val configProvider: Single<BloodPressureConfig>,
    private val dateValidator: DateOfBirthFormatValidator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .compose(combineDateInputs())
        .compose(validateBpInput())
        .compose(validateDateInput())
        .replay()

    return Observable.mergeArray(
        automaticFocusChanges(replayedEvents),
        validationErrorResets(replayedEvents),
        prefillWhenUpdatingABloodPressure(replayedEvents),
        showBpValidationErrors(replayedEvents),
        proceedToDateEntryWhenBpEntryIsDone(replayedEvents),
        //        updateBp(replayedEvents),
        toggleRemoveBloodPressureButton(replayedEvents),
        updateSheetTitle(replayedEvents),
        showConfirmRemoveBloodPressureDialog(replayedEvents),
        closeSheetWhenEditedBpIsDeleted(replayedEvents),
        showDateValidationErrors(replayedEvents),
        saveBpWhenDateEntryIsDone(replayedEvents))
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
        .map { { ui: Ui -> ui.hideErrorMessage() } }

    val diastolicChanges = events.ofType<BloodPressureDiastolicTextChanged>()
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.hideErrorMessage() } }

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

  private fun showBpValidationErrors(events: Observable<UiEvent>): Observable<UiChange> =
      events.ofType<BloodPressureBpValidated>()
          .map {
            { ui: Ui ->
              when (it.result) {
                ErrorSystolicLessThanDiastolic -> ui.showSystolicLessThanDiastolicError()
                ErrorSystolicTooHigh -> ui.showSystolicHighError()
                ErrorSystolicTooLow -> ui.showSystolicLowError()
                ErrorDiastolicTooHigh -> ui.showDiastolicHighError()
                ErrorDiastolicTooLow -> ui.showDiastolicLowError()
                ErrorSystolicEmpty -> ui.showSystolicEmptyError()
                ErrorDiastolicEmpty -> ui.showDiastolicEmptyError()
                Success -> {
                  // Nothing to do here, SUCCESS handled below separately!
                }
              }.exhaustive()
            }
          }

  private fun proceedToDateEntryWhenBpEntryIsDone(events: Observable<UiEvent>): Observable<UiChange> =
      events.ofType<BloodPressureBpValidated>()
          .filter { it.result is Success }
          .map { Ui::showDateEntryScreen }

  private fun validateBpInput() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val screenChanges = events
        .ofType<BloodPressureScreenChanged>()
        .map { it.type }

    val saveBpClicks = events
        .ofType<BloodPressureSaveClicked>()
        .withLatestFrom(screenChanges)
        .filter { (_, screen) -> screen == ScreenType.BP_ENTRY }

    val systolicChanges = events
        .ofType<BloodPressureSystolicTextChanged>()
        .map { it.systolic }

    val diastolicChanges = events
        .ofType<BloodPressureDiastolicTextChanged>()
        .map { it.diastolic }

    val validations = saveBpClicks
        .withLatestFrom(systolicChanges, diastolicChanges)
        .map { (_, systolic, diastolic) -> BloodPressureBpValidated(bpResult(systolic, diastolic)) }

    events.mergeWith(validations)
  }

  private fun bpResult(systolic: String, diastolic: String): Validation {
    if (systolic.isBlank()) {
      return ErrorSystolicEmpty
    }
    if (diastolic.isBlank()) {
      return ErrorDiastolicEmpty
    }

    val systolicNumber = systolic.trim().toInt()
    val diastolicNumber = diastolic.trim().toInt()

    return when {
      systolicNumber < 70 -> ErrorSystolicTooLow
      systolicNumber > 300 -> ErrorSystolicTooHigh
      diastolicNumber < 40 -> ErrorDiastolicTooLow
      diastolicNumber > 180 -> ErrorDiastolicTooHigh
      systolicNumber < diastolicNumber -> ErrorSystolicLessThanDiastolic
      else -> Success
    }
  }

  sealed class Validation {
    object Success : Validation()
    object ErrorSystolicEmpty : Validation()
    object ErrorDiastolicEmpty : Validation()
    object ErrorSystolicTooHigh : Validation()
    object ErrorSystolicTooLow : Validation()
    object ErrorDiastolicTooHigh : Validation()
    object ErrorDiastolicTooLow : Validation()
    object ErrorSystolicLessThanDiastolic : Validation()
  }

  private fun toggleRemoveBloodPressureButton(events: Observable<UiEvent>): Observable<UiChange> {
    val featureEnabledStream = configProvider
        .map { it.deleteBloodPressureFeatureEnabled }
        .toObservable()

    val openAsStream = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.openAs }

    val hideRemoveBpButton = Observables
        .combineLatest(featureEnabledStream, openAsStream)
        .filter { (featureEnabled, openAs) -> featureEnabled.not() || openAs is OpenAs.New }
        .map { { ui: Ui -> ui.hideRemoveBpButton() } }

    val showRemoveBpButton = Observables
        .combineLatest(featureEnabledStream, openAsStream)
        .filter { (featureEnabled, openAs) -> featureEnabled && openAs is OpenAs.Update }
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

  private fun combineDateInputs() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val dayChanges = events
        .ofType<BloodPressureDayChanged>()
        .map { it.day }

    val monthChanges = events
        .ofType<BloodPressureMonthChanged>()
        .map { it.month }

    val yearChanges = events
        .ofType<BloodPressureYearChanged>()
        .map { it.year }

    val combinedDates = Observables
        .combineLatest(dayChanges, monthChanges, yearChanges)
        .map { (d, m, y) -> BloodPressureDateChanged(date = "$d/$m/$y") }
    events.mergeWith(combinedDates)
  }

  private fun validateDateInput() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val screenChanges = events
        .ofType<BloodPressureScreenChanged>()
        .filter { it.type == DATE_ENTRY }

    val saveBpClicks = events.ofType<BloodPressureSaveClicked>()
        .withLatestFrom(screenChanges)

    val dateChanges = events
        .ofType<BloodPressureDateChanged>()
        .map { it.date }

    val validations = saveBpClicks
        .withLatestFrom(dateChanges) { _, date ->
          BloodPressureDateValidated(date = date)
        }
    events.mergeWith(validations)
  }

  private fun showDateValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<BloodPressureDateValidated>()
        .map { it.result(dateValidator) }
        .ofType<Invalid>()
        .map<UiChange> {
          when (it) {
            is InvalidPattern -> { ui: Ui -> ui.showInvalidDateError() }
            is DateIsInFuture -> { ui: Ui -> ui.showDateIsInFutureError() }
          }
        }
  }

  private fun saveBpWhenDateEntryIsDone(events: Observable<UiEvent>): Observable<UiChange> {
    val openAs = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.openAs }

    val validDates = events
        .ofType<BloodPressureDateValidated>()
        .map { it.result(dateValidator) }
        .ofType<Valid>()
        .map { it.parsedDate }

    data class BP(val systolic: Int, val diastolic: Int)

    val systolicChanges = events
        .ofType<BloodPressureSystolicTextChanged>()
        .map { it.systolic }

    val diastolicChanges = events
        .ofType<BloodPressureDiastolicTextChanged>()
        .map { it.diastolic }

    val bpChanges = validDates
        .withLatestFrom(systolicChanges, diastolicChanges)
        .map { (_, systolic, diastolic) -> BP(systolic.toInt(), diastolic.toInt()) }

    val patientUuidStream = openAs
        .ofType<OpenAs.New>()
        .map { it.patientUuid }

    val existingBpUuidStream = openAs
        .ofType<OpenAs.Update>()
        .map { it.bpUuid }

    val saveNewBp = validDates
        .withLatestFrom(bpChanges, patientUuidStream)
        .flatMapSingle { (date, bp, patientUuid) ->
          val dateAsInstant = date.atStartOfDay(UTC).toInstant()
          bloodPressureRepository.saveMeasurement(patientUuid, bp.systolic, bp.diastolic, dateAsInstant)
        }
        .map { { ui: Ui -> ui.setBpSavedResultAndFinish() } }

    val updateExistingBp = validDates
        .withLatestFrom(bpChanges, existingBpUuidStream)
        .flatMap { (date, newBp, existingBpUuid) ->
          bloodPressureRepository.measurement(existingBpUuid)
              .take(1)
              .map { existingBp ->
                val dateAsInstant = date.atStartOfDay(UTC).toInstant()
                existingBp.copy(
                    systolic = newBp.systolic,
                    diastolic = newBp.diastolic,
                    createdAt = dateAsInstant,
                    updatedAt = dateAsInstant)
              }
              .flatMapCompletable { bloodPressureRepository.updateMeasurement(it) }
              .andThen(Observable.just({ ui: Ui -> ui.setBpSavedResultAndFinish() }))
        }

    return saveNewBp.mergeWith(updateExistingBp)
  }
}
