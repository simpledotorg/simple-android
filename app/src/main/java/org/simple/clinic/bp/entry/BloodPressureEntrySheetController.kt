package org.simple.clinic.bp.entry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.LocalDate
import javax.inject.Inject

typealias Ui = BloodPressureEntryUi
typealias UiChange = (Ui) -> Unit

/**
 * V2: Includes date entry.
 */
class BloodPressureEntrySheetController @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
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
        .replay()

    return dismissSheetWhenBpIsSaved(replayedEvents)
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

  private fun validateBpInput() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val bpEntryScreenChanges = events
        .ofType<ScreenChanged>()
        .map { it.type }
        .filter { it == BP_ENTRY }

    val systolicChanges = events
        .ofType<SystolicChanged>()
        .map { it.systolic }

    val diastolicChanges = events
        .ofType<DiastolicChanged>()
        .map { it.diastolic }

    val validations = Observables.combineLatest(systolicChanges, diastolicChanges, bpEntryScreenChanges)
        .map { (systolic, diastolic, _) -> bpValidator.validate(systolic, diastolic) }
        .map(::BloodPressureReadingsValidated)

    events.mergeWith(validations)
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

  private fun dismissSheetWhenBpIsSaved(events: Observable<UiEvent>): Observable<UiChange> {
    return events.flatMap { Observable.never<UiChange>() }
  }
}
