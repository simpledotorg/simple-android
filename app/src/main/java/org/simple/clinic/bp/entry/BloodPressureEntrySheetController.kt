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
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update
import org.simple.clinic.bp.entry.SaveBpData.NeedsCorrection
import org.simple.clinic.bp.entry.SaveBpData.ReadyToCreate
import org.simple.clinic.bp.entry.SaveBpData.ReadyToUpdate
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.toUtcInstant
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
import org.threeten.bp.LocalDate
import javax.inject.Inject

typealias Ui = BloodPressureEntryUi
typealias UiChange = (Ui) -> Unit

/**
 * V2: Includes date entry.
 */
class BloodPressureEntrySheetController @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val dateValidator: UserInputDateValidator,
    private val bpValidator: BpValidator,
    private val userClock: UserClock,
    private val inputDatePaddingCharacter: UserInputDatePaddingCharacter,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(combineDateInputs())
        .compose(validateBpInput())
        .compose(validateDateInput())
        .compose(calculateDateToPrefill())
        .compose(saveBpWhenDateEntryIsDone())
        .replay()

    return Observable.mergeArray(
        automaticFocusChanges(replayedEvents),
        prefillBpWhenUpdatingABloodPressure(replayedEvents),
        prefillDate(replayedEvents),
        showBpValidationErrors(replayedEvents),
        hideBpValidationErrors(replayedEvents),
        proceedToDateEntryWhenBpEntryIsDone(replayedEvents),
        showBpEntry(replayedEvents),
        dismissSheetWhenBackIsPressedOnBp(replayedEvents),
        toggleRemoveBloodPressureButton(replayedEvents),
        updateSheetTitle(replayedEvents),
        showConfirmRemoveBloodPressureDialog(replayedEvents),
        closeSheetWhenEditedBpIsDeleted(replayedEvents),
        showDateValidationErrors(replayedEvents),
        hideDateValidationErrors(replayedEvents),
        dismissSheetWhenBpIsSaved(replayedEvents)
    )
  }

  private fun automaticFocusChanges(events: Observable<UiEvent>): Observable<UiChange> {
    val isSystolicValueComplete = { systolicText: String ->
      (systolicText.length == 3 && systolicText.matches("^[123].*$".toRegex()))
          || (systolicText.length == 2 && systolicText.matches("^[789].*$".toRegex()))
    }

    val moveFocusToDiastolic = events.ofType<SystolicChanged>()
        .distinctUntilChanged()
        .filter { isSystolicValueComplete(it.systolic) }
        .map { { ui: Ui -> ui.changeFocusToDiastolic() } }

    data class BloodPressure(val systolic: String, val diastolic: String)

    val systolicChanges = events.ofType<SystolicChanged>().map { it.systolic }
    val diastolicChanges = events.ofType<DiastolicChanged>().map { it.diastolic }

    val bpChanges = Observables
        .combineLatest(systolicChanges, diastolicChanges)
        .map { (systolic, diastolic) -> BloodPressure(systolic, diastolic) }

    val diastolicBackspaceClicksWithEmptyText = events.ofType<DiastolicBackspaceClicked>()
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
    val systolicChanges = events.ofType<SystolicChanged>()
    val diastolicChanges = events.ofType<DiastolicChanged>()

    return Observable
        .merge(systolicChanges, diastolicChanges)
        .map { { ui: Ui -> ui.hideBpErrorMessage() } }
  }

  private fun prefillBpWhenUpdatingABloodPressure(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SheetCreated>()
        .filter { it.openAs is Update }
        .map { it.openAs as Update }
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
        .ofType<SheetCreated>()
        .map { it.openAs }

    val dateForNewBp = openAsStream
        .ofType<New>()
        .map { LocalDate.now(userClock) }

    val dateForExistingBp = openAsStream
        .ofType<Update>()
        .flatMap { bloodPressureRepository.measurement(it.bpUuid) }
        .take(1)
        .map { it.recordedAt.atZone(userClock.zone).toLocalDate() }

    val prefillDateEvent = dateForNewBp
        .mergeWith(dateForExistingBp)
        .map { DateToPrefillCalculated(it) }

    events.mergeWith(prefillDateEvent)
  }

  private fun prefillDate(events: Observable<UiEvent>): Observable<UiChange> {
    val localDates = events
        .ofType<DateToPrefillCalculated>()
        .map { it.date }

    val updates = events
        .ofType<SheetCreated>()
        .filter { it.openAs is Update }

    return Observables
        .combineLatest(localDates, updates) { date, _ -> date }
        .map { date ->
          { ui: Ui ->
            val dayString = date.dayOfMonth.toString().padStart(length = 2, padChar = inputDatePaddingCharacter.value)
            val monthString = date.monthValue.toString().padStart(length = 2, padChar = inputDatePaddingCharacter.value)
            val yearString = date.year.toString().substring(startIndex = 2, endIndex = 4)
            ui.setDateOnInputFields(dayString, monthString, yearString)
            ui.showDateOnDateButton(date)
          }
        }.take(1)
  }

  private fun showBpValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    val saveClicks = Observable.merge(
        events.ofType<BloodPressureDateClicked>(),
        events.ofType<SaveClicked>())

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
    val screenChanges = events
        .ofType<ScreenChanged>()
        .map { it.type }

    val validations = events
        .ofType<BloodPressureReadingsValidated>()
        .map { it.result }

    return events
        .ofType<BloodPressureDateClicked>()
        .withLatestFrom(screenChanges, validations)
        .filter { (_, screen, result) -> screen == BP_ENTRY && result is Success }
        .map { Ui::showDateEntryScreen }
  }

  private fun showBpEntry(events: Observable<UiEvent>): Observable<UiChange> {
    val screenChanges = events
        .ofType<ScreenChanged>()
        .map { it.type }

    val backPresses = events
        .ofType<BackPressed>()
        .withLatestFrom(screenChanges)
        .filter { (_, screen) -> screen == DATE_ENTRY }

    val mergedBackClicks = Observable.merge(
        events.ofType<ShowBpClicked>(),
        backPresses
    )

    val validateDateEvents = mergedBackClicks
        .withLatestFrom(events.ofType<DateValidated>()) { _, validated -> validated.result }
        .ofType<Valid>()

    return validateDateEvents
        .map { { ui: Ui -> ui.showBpEntryScreen() } }
  }

  private fun dismissSheetWhenBackIsPressedOnBp(events: Observable<UiEvent>): Observable<UiChange> {
    val screenChanges = events
        .ofType<ScreenChanged>()
        .map { it.type }

    return events
        .ofType<BackPressed>()
        .withLatestFrom(screenChanges)
        .filter { (_, screen) -> screen == BP_ENTRY }
        .map { { ui: Ui -> ui.dismiss() } }
  }

  private fun validateBpInput() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val screenChanges = events
        .ofType<ScreenChanged>()
        .map { it.type }

    val systolicChanges = events
        .ofType<SystolicChanged>()
        .map { it.systolic }

    val diastolicChanges = events
        .ofType<DiastolicChanged>()
        .map { it.diastolic }

    val validations = Observables.combineLatest(systolicChanges, diastolicChanges, screenChanges)
        .filter { (_, _, screen) -> screen == BP_ENTRY }
        .map { (systolic, diastolic, _) -> bpValidator.validate(systolic, diastolic) }
        .map(::BloodPressureReadingsValidated)

    events.mergeWith(validations)
  }

  private fun toggleRemoveBloodPressureButton(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SheetCreated>()
        .map { it.openAs }
        .ofType<Update>()
        .map { { ui: Ui -> ui.showRemoveBpButton() } }
  }

  private fun updateSheetTitle(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<SheetCreated>()
        .map { it.openAs }
        .filter { it is Update }
        .map { { ui: Ui -> ui.showEditBloodPressureTitle() } }
  }

  private fun showConfirmRemoveBloodPressureDialog(events: Observable<UiEvent>): Observable<UiChange> {
    val bloodPressureMeasurementUuidStream = events
        .ofType<SheetCreated>()
        .map { it.openAs }
        .ofType<Update>()
        .map { it.bpUuid }

    val removeClicks = events.ofType<RemoveClicked>()

    return removeClicks
        .withLatestFrom(bloodPressureMeasurementUuidStream)
        .map { (_, uuid) -> { ui: Ui -> ui.showConfirmRemoveBloodPressureDialog(uuid) } }
  }

  private fun closeSheetWhenEditedBpIsDeleted(events: Observable<UiEvent>): Observable<UiChange> {
    val bloodPressureMeasurementUuidStream = events
        .ofType<SheetCreated>()
        .map { it.openAs }
        .ofType<Update>()
        .map { it.bpUuid }

    return bloodPressureMeasurementUuidStream
        .flatMap(bloodPressureRepository::measurement)
        .filter { it.deletedAt != null }
        .take(1)
        .map { { ui: Ui -> ui.setBpSavedResultAndFinish() } }
  }

  private fun combineDateInputs() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val dayChanges = events
        .ofType<DayChanged>()
        .map { it.day }

    val monthChanges = events
        .ofType<MonthChanged>()
        .map { it.month }

    val yearChanges = events
        .ofType<YearChanged>()
        .map { it.twoDigitYear }

    val combinedDates = Observables
        .combineLatest(dayChanges, monthChanges, yearChanges)
        .map { (dd, mm, yy) ->
          val paddedDd = dd.padStart(length = 2, padChar = inputDatePaddingCharacter.value)
          val paddedMm = mm.padStart(length = 2, padChar = inputDatePaddingCharacter.value)
          val paddedYy = yy.padStart(length = 2, padChar = inputDatePaddingCharacter.value)

          val firstTwoDigitsOfYear = LocalDate.now(userClock).year.toString().substring(0, 2)
          val paddedYyyy = firstTwoDigitsOfYear + paddedYy
          DateChanged(date = "$paddedDd/$paddedMm/$paddedYyyy")
        }
    events.mergeWith(combinedDates)
  }

  private fun validateDateInput() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val screenChanges = events
        .ofType<ScreenChanged>()

    val dateChanges = events
        .ofType<DateChanged>()
        .map { it.date }

    val validations = Observables.combineLatest(screenChanges, dateChanges)
        .map { (_, date) ->
          val validationResult = dateValidator.validate(date)
          DateValidated(date, validationResult)
        }

    events.mergeWith(validations)
  }

  private fun showDateValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    val saveClicks = events.ofType<SaveClicked>()
    val showBpClicks = events.ofType<ShowBpClicked>()
    val backPresses = events.ofType<BackPressed>()

    val validations = events
        .ofType<DateValidated>()
        .map { it.result }

    return Observable
        .merge(saveClicks, showBpClicks, backPresses)
        .withLatestFrom(validations)
        .map { (_, result) ->
          when (result) {
            is InvalidPattern -> { ui: Ui -> ui.showInvalidDateError() }
            is DateIsInFuture -> { ui: Ui -> ui.showDateIsInFutureError() }
            is Valid -> { ui: Ui -> ui.showDateOnDateButton(result.parsedDate) }
          }
        }
  }

  private fun hideDateValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<DateChanged>()
        .map { { ui: Ui -> ui.hideDateErrorMessage() } }
  }

  private fun saveBpWhenDateEntryIsDone() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val openAs = events
        .ofType<SheetCreated>()
        .map { it.openAs }

    val dateValidations = events
        .ofType<DateValidated>()
        .map { it.result }

    val bpValidations = events
        .ofType<BloodPressureReadingsValidated>()
        .map { it.result }

    val patientUuidStream = openAs
        .ofType<New>()
        .map { it.patientUuid }

    val existingBpUuidStream = openAs
        .ofType<Update>()
        .map { it.bpUuid }

    val saveClicks = events
        .ofType<SaveClicked>()

    val prefilledDateStream = events
        .ofType<DateToPrefillCalculated>()
        .map { it.date }

    val loggedInUserStream = userSession
        .requireLoggedInUser()
        .replay()
        .refCount()

    val currentFacilityStream = loggedInUserStream
        .flatMap(facilityRepository::currentFacility)
        .replay()
        .refCount()

    val newBpDataStream = Observables
        .combineLatest(
            dateValidations,
            bpValidations,
            patientUuidStream,
            loggedInUserStream,
            currentFacilityStream
        ) { dateResult, bpResult, patientUuid, loggedInUser, currentFacility ->
          if (dateResult is Valid && bpResult is Success) {
            ReadyToCreate(
                date = dateResult.parsedDate,
                systolic = bpResult.systolic,
                diastolic = bpResult.diastolic,
                patientUuid = patientUuid,
                loggedInUser = loggedInUser,
                currentFacility = currentFacility
            )
          } else {
            NeedsCorrection
          }
        }

    val updateBpDataStream = Observables.combineLatest(
        dateValidations,
        bpValidations,
        existingBpUuidStream,
        loggedInUserStream,
        currentFacilityStream
    ) { dateResult, bpResult, bpUuid, loggedInUser, currentFacility ->
      if (dateResult is Valid && bpResult is Success) {
        ReadyToUpdate(
            date = dateResult.parsedDate,
            systolic = bpResult.systolic,
            diastolic = bpResult.diastolic,
            bpUuid = bpUuid,
            loggedInUser = loggedInUser,
            currentFacility = currentFacility
        )
      } else {
        NeedsCorrection
      }
    }

    val saveNewBp = saveClicks
        .withLatestFrom(newBpDataStream) { _, newBp -> newBp }
        .ofType<ReadyToCreate>()
        .withLatestFrom(prefilledDateStream)
        .flatMapSingle { (newBp, prefilledDate) ->
          bloodPressureRepository
              .saveMeasurement(
                  patientUuid = newBp.patientUuid,
                  systolic = newBp.systolic,
                  diastolic = newBp.diastolic,
                  recordedAt = newBp.date.toUtcInstant(userClock),
                  loggedInUser = newBp.loggedInUser,
                  currentFacility = newBp.currentFacility
              )
              .map { BloodPressureSaved(wasDateChanged = prefilledDate != newBp.date) }
              .flatMap { bpSaved ->
                appointmentRepository
                    .markAppointmentsCreatedBeforeTodayAsVisited(newBp.patientUuid)
                    .andThen(patientRepository.compareAndUpdateRecordedAt(newBp.patientUuid, newBp.date.toUtcInstant(userClock)))
                    .toSingleDefault(bpSaved)
              }
        }

    val updateExistingBp = saveClicks
        .withLatestFrom(updateBpDataStream) { _, updateBp -> updateBp }
        .ofType<ReadyToUpdate>()
        .withLatestFrom(prefilledDateStream)
        .flatMapSingle { (updateBp, prefilledDate) ->
          bloodPressureRepository.measurement(updateBp.bpUuid)
              .firstOrError()
              .map { existingBp ->
                existingBp.copy(
                    systolic = updateBp.systolic,
                    diastolic = updateBp.diastolic,
                    recordedAt = updateBp.date.toUtcInstant(userClock),
                    userUuid = updateBp.loggedInUser.uuid,
                    facilityUuid = updateBp.currentFacility.uuid
                )
              }
              .flatMapCompletable {
                bloodPressureRepository.updateMeasurement(it)
                    .andThen(patientRepository.compareAndUpdateRecordedAt(it.patientUuid, it.recordedAt))
              }
              .andThen(Single.just(BloodPressureSaved(wasDateChanged = prefilledDate != updateBp.date)))
        }

    events.mergeWith(saveNewBp).mergeWith(updateExistingBp)
  }

  private fun dismissSheetWhenBpIsSaved(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<BloodPressureSaved>()
        .map { { ui: Ui -> ui.setBpSavedResultAndFinish() } }
  }
}
