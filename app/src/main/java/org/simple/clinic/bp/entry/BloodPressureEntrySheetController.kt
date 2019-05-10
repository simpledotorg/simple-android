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
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.DATE_ENTRY
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicLessThanDiastolic
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.Success
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result2.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result2.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result2.Valid
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneOffset.UTC
import java.util.UUID
import javax.inject.Inject

typealias Ui = BloodPressureEntrySheet
typealias UiChange = (Ui) -> Unit

/**
 * V2: Includes date entry.
 */
class BloodPressureEntrySheetController @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val appointmentRepository: AppointmentRepository,
    private val dateValidator: UserInputDateValidator,
    private val bpValidator: BpValidator,
    private val userClock: UserClock,
    private val inputDatePaddingCharacter: UserInputDatePaddingCharacter
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(combineDateInputs())
        .compose(validateBpInput())
        .compose(validateDateInput())
        .compose(calculateDateToPrefill())
        .compose(saveBpWhenDateEntryIsDone())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        automaticFocusChanges(replayedEvents),
        prefillBpWhenUpdatingABloodPressure(replayedEvents),
        prefillDate(replayedEvents),
        showBpValidationErrors(replayedEvents),
        hideBpValidationErrors(replayedEvents),
        proceedToDateEntryWhenBpEntryIsDone(replayedEvents),
        showBpEntryWhenPreviousArrowIsPressed(replayedEvents),
        dismissSheetWhenBackIsPressedOnBp(replayedEvents),
        enableNextArrowWhileBpIsValid(replayedEvents),
        toggleRemoveBloodPressureButton(replayedEvents),
        updateSheetTitle(replayedEvents),
        showConfirmRemoveBloodPressureDialog(replayedEvents),
        closeSheetWhenEditedBpIsDeleted(replayedEvents),
        showDateValidationErrors(replayedEvents),
        hideDateValidationErrors(replayedEvents),
        dismissSheetWhenBpIsSaved(replayedEvents))
  }

  private fun automaticFocusChanges(events: Observable<UiEvent>): Observable<UiChange> {
    val isSystolicValueComplete = { systolicText: String ->
      (systolicText.length == 3 && systolicText.matches("^[123].*$".toRegex()))
          || (systolicText.length == 2 && systolicText.matches("^[789].*$".toRegex()))
    }

    val moveFocusToDiastolic = events.ofType<BloodPressureSystolicTextChanged>()
        .distinctUntilChanged()
        .filter { isSystolicValueComplete(it.systolic) }
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

  private fun hideBpValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    val systolicChanges = events.ofType<BloodPressureSystolicTextChanged>()
    val diastolicChanges = events.ofType<BloodPressureDiastolicTextChanged>()

    return Observable
        .merge(systolicChanges, diastolicChanges)
        .map { { ui: Ui -> ui.hideBpErrorMessage() } }
  }

  private fun prefillBpWhenUpdatingABloodPressure(events: Observable<UiEvent>): Observable<UiChange> {
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

  private fun calculateDateToPrefill() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val openAsStream = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.openAs }

    val dateForNewBp = openAsStream
        .ofType<OpenAs.New>()
        .map { LocalDate.now(userClock) }

    val dateForExistingBp = openAsStream
        .ofType<OpenAs.Update>()
        .flatMap { bloodPressureRepository.measurement(it.bpUuid) }
        .take(1)
        .map { it.recordedAt.atZone(userClock.zone).toLocalDate() }

    val prefillDateEvent = dateForNewBp
        .mergeWith(dateForExistingBp)
        .map { BloodPressureDateToPrefillCalculated(it) }

    events.mergeWith(prefillDateEvent)
  }

  private fun prefillDate(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<BloodPressureDateToPrefillCalculated>()
        .map { it.date }
        .map { date ->
          { ui: Ui ->
            val dayString = date.dayOfMonth.toString().padStart(length = 2, padChar = inputDatePaddingCharacter.value)
            val monthString = date.monthValue.toString().padStart(length = 2, padChar = inputDatePaddingCharacter.value)
            val yearString = date.year.toString().substring(startIndex = 2, endIndex = 4)
            ui.setDate(dayString, monthString, yearString)
          }
        }
  }

  private fun showBpValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    val saveClicks = Observable.merge(
        events.ofType<BloodPressureNextArrowClicked>(),
        events.ofType<BloodPressureSaveClicked>())

    val validations = events
        .ofType<BloodPressureReadingsValidated>()
        .map { it.result }

    return saveClicks
        .withLatestFrom(validations)
        .map { (_, result) ->
          { ui: Ui ->
            when (result) {
              is ErrorSystolicLessThanDiastolic -> ui.showSystolicLessThanDiastolicError()
              is ErrorSystolicTooHigh -> ui.showSystolicHighError()
              is ErrorSystolicTooLow -> ui.showSystolicLowError()
              is ErrorDiastolicTooHigh -> ui.showDiastolicHighError()
              is ErrorDiastolicTooLow -> ui.showDiastolicLowError()
              is ErrorSystolicEmpty -> ui.showSystolicEmptyError()
              is ErrorDiastolicEmpty -> ui.showDiastolicEmptyError()
              is Success -> {
                // Nothing to do here.
              }
            }.exhaustive()
          }
        }
  }

  private fun proceedToDateEntryWhenBpEntryIsDone(events: Observable<UiEvent>): Observable<UiChange> {
    val saveClicks = Observable.merge(
        events.ofType<BloodPressureNextArrowClicked>(),
        events.ofType<BloodPressureSaveClicked>())

    val screenChanges = events
        .ofType<BloodPressureScreenChanged>()
        .map { it.type }

    val validations = events
        .ofType<BloodPressureReadingsValidated>()
        .map { it.result }

    return saveClicks
        .withLatestFrom(screenChanges, validations)
        .filter { (_, screen, result) -> screen == BP_ENTRY && result is Success }
        .map { Ui::showDateEntryScreen }
  }

  private fun showBpEntryWhenPreviousArrowIsPressed(events: Observable<UiEvent>): Observable<UiChange> {
    val previousArrowClicks = events
        .ofType<BloodPressurePreviousArrowClicked>()

    val screenChanges = events
        .ofType<BloodPressureScreenChanged>()
        .map { it.type }

    val backPresses = events
        .ofType<BloodPressureBackPressed>()
        .withLatestFrom(screenChanges)
        .filter { (_, screen) -> screen == DATE_ENTRY }

    return Observable
        .merge(previousArrowClicks, backPresses)
        .map { { ui: Ui -> ui.showBpEntryScreen() } }
  }

  private fun dismissSheetWhenBackIsPressedOnBp(events: Observable<UiEvent>): Observable<UiChange> {
    val screenChanges = events
        .ofType<BloodPressureScreenChanged>()
        .map { it.type }

    return events
        .ofType<BloodPressureBackPressed>()
        .withLatestFrom(screenChanges)
        .filter { (_, screen) -> screen == BP_ENTRY }
        .map { { ui: Ui -> ui.finish() } }
  }

  private fun enableNextArrowWhileBpIsValid(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<BloodPressureReadingsValidated>()
        .map { it.result is Success }
        .distinctUntilChanged()
        .map { isBpValid -> { ui: Ui -> ui.setNextArrowIconEnabled(isBpValid) } }
  }

  private fun validateBpInput() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val screenChanges = events
        .ofType<BloodPressureScreenChanged>()
        .map { it.type }

    val systolicChanges = events
        .ofType<BloodPressureSystolicTextChanged>()
        .map { it.systolic }

    val diastolicChanges = events
        .ofType<BloodPressureDiastolicTextChanged>()
        .map { it.diastolic }

    val validations = Observables.combineLatest(systolicChanges, diastolicChanges, screenChanges)
        .filter { (_, _, screen) -> screen == BP_ENTRY }
        .map { (systolic, diastolic, _) -> bpValidator.validate(systolic, diastolic) }
        .map(::BloodPressureReadingsValidated)

    events.mergeWith(validations)
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

  private fun updateSheetTitle(events: Observable<UiEvent>): Observable<UiChange> {
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

  private fun showConfirmRemoveBloodPressureDialog(events: Observable<UiEvent>): Observable<UiChange> {
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

  private fun closeSheetWhenEditedBpIsDeleted(events: Observable<UiEvent>): Observable<UiChange> {
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
        .map { it.twoDigitYear }

    val combinedDates = Observables
        .combineLatest(dayChanges, monthChanges, yearChanges)
        .map { (dd, mm, yy) ->
          val paddedDd = dd.padStart(length = 2, padChar = inputDatePaddingCharacter.value)
          val paddedMm = mm.padStart(length = 2, padChar = inputDatePaddingCharacter.value)
          val paddedYy = yy.padStart(length = 2, padChar = inputDatePaddingCharacter.value)

          val firstTwoDigitsOfYear = LocalDate.now(userClock).year.toString().substring(0, 2)
          val paddedYyyy = firstTwoDigitsOfYear + paddedYy
          BloodPressureDateChanged(date = "$paddedDd/$paddedMm/$paddedYyyy")
        }
    events.mergeWith(combinedDates)
  }

  private fun validateDateInput() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val screenChanges = events
        .ofType<BloodPressureScreenChanged>()

    val dateChanges = events
        .ofType<BloodPressureDateChanged>()
        .map { it.date }

    val validations = Observables.combineLatest(screenChanges, dateChanges)
        .filter { (screen) -> screen.type == DATE_ENTRY }
        .map { (_, date) ->
          val validationResult = dateValidator.validate2(date)
          BloodPressureDateValidated(date, validationResult)
        }

    events.mergeWith(validations)
  }

  private fun showDateValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    val saveClicks = events.ofType<BloodPressureSaveClicked>()

    val validations = events
        .ofType<BloodPressureDateValidated>()
        .map { it.result }

    return saveClicks
        .withLatestFrom(validations)
        .map<UiChange> { (_, result) ->
          when (result) {
            is InvalidPattern -> { ui: Ui -> ui.showInvalidDateError() }
            is DateIsInFuture -> { ui: Ui -> ui.showDateIsInFutureError() }
            is Valid -> { ui: Ui ->
              // Nothing to do here.
            }
          }
        }
  }

  private fun hideDateValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<BloodPressureDateChanged>()
        .map { { ui: Ui -> ui.hideDateErrorMessage() } }
  }

  private fun saveBpWhenDateEntryIsDone() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val openAs = events
        .ofType<BloodPressureEntrySheetCreated>()
        .map { it.openAs }

    val dateValidations = events
        .ofType<BloodPressureDateValidated>()
        .map { it.result }

    val bpValidations = events
        .ofType<BloodPressureReadingsValidated>()
        .map { it.result }

    val patientUuidStream = openAs
        .ofType<OpenAs.New>()
        .map { it.patientUuid }

    val existingBpUuidStream = openAs
        .ofType<OpenAs.Update>()
        .map { it.bpUuid }

    val screenChanges = events
        .ofType<BloodPressureScreenChanged>()
        .map { it.type }

    val saveClicks = events
        .ofType<BloodPressureSaveClicked>()
        .withLatestFrom(screenChanges)
        .filter { (_, screen) -> screen == DATE_ENTRY }

    val prefilledDateStream = events
        .ofType<BloodPressureDateToPrefillCalculated>()
        .map { it.date }

    val newBpDataStream = Observables.combineLatest(dateValidations, bpValidations, patientUuidStream)
        .map { (dateResult, bpResult, patientUuid) ->
          if (dateResult is Valid && bpResult is Success) {
            SaveBpData.ReadyToCreate(dateResult.parsedDate, bpResult.systolic, bpResult.diastolic, patientUuid)
          } else {
            SaveBpData.NeedsCorrection
          }
        }

    val updateBpDataStream = Observables.combineLatest(dateValidations, bpValidations, existingBpUuidStream)
        .map { (dateResult, bpResult, bpUuid) ->
          if (dateResult is Valid && bpResult is Success) {
            SaveBpData.ReadyToUpdate(dateResult.parsedDate, bpResult.systolic, bpResult.diastolic, bpUuid)
          } else {
            SaveBpData.NeedsCorrection
          }
        }

    val toUtcInstant = { date: LocalDate ->
      val userTime = LocalTime.now(userClock)
      val userDateTime = date.atTime(userTime)
      val utcDateTime = userDateTime.atZone(userClock.zone).withZoneSameInstant(UTC)
      utcDateTime.toInstant()
    }

    val saveNewBp = saveClicks
        .withLatestFrom(newBpDataStream) { _, newBp -> newBp }
        .ofType<SaveBpData.ReadyToCreate>()
        .withLatestFrom(prefilledDateStream)
        .flatMapSingle { (newBp, prefilledDate) ->
          bloodPressureRepository
              .saveMeasurement(newBp.patientUuid, newBp.systolic, newBp.diastolic, toUtcInstant(newBp.date))
              .map { BloodPressureSaved(wasDateChanged = prefilledDate != newBp.date) }
              .flatMap { bpSaved ->
                appointmentRepository
                    .markAppointmentsCreatedBeforeTodayAsVisited(newBp.patientUuid)
                    .toSingleDefault(bpSaved)
              }
        }

    val updateExistingBp = saveClicks
        .withLatestFrom(updateBpDataStream) { _, updateBp -> updateBp }
        .ofType<SaveBpData.ReadyToUpdate>()
        .withLatestFrom(prefilledDateStream)
        .flatMapSingle { (updateBp, prefilledDate) ->
          bloodPressureRepository.measurement(updateBp.bpUuid)
              .firstOrError()
              .map { existingBp ->
                existingBp.copy(
                    systolic = updateBp.systolic,
                    diastolic = updateBp.diastolic,
                    recordedAt = toUtcInstant(updateBp.date))
              }
              .flatMapCompletable { bloodPressureRepository.updateMeasurement(it) }
              .andThen(Single.just(BloodPressureSaved(wasDateChanged = prefilledDate != updateBp.date)))
        }

    events.mergeWith(saveNewBp).mergeWith(updateExistingBp)
  }

  sealed class SaveBpData {
    data class ReadyToCreate(val date: LocalDate, val systolic: Int, val diastolic: Int, val patientUuid: PatientUuid) : SaveBpData()
    data class ReadyToUpdate(val date: LocalDate, val systolic: Int, val diastolic: Int, val bpUuid: UUID) : SaveBpData()
    object NeedsCorrection : SaveBpData()
  }

  private fun dismissSheetWhenBpIsSaved(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<BloodPressureSaved>()
        .map { { ui: Ui -> ui.setBpSavedResultAndFinish() } }
  }
}
