package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarEmpty
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooHigh
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooLow
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

class BloodSugarEntryEffectHandler(
    private val ui: BloodSugarEntryUi,
    private val userClock: UserClock,
    private val schedulersProvider: SchedulersProvider
) {
  fun build(): ObservableTransformer<BloodSugarEntryEffect, BloodSugarEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarEntryEffect, BloodSugarEntryEvent>()
        .addAction(HideBloodSugarErrorMessage::class.java, ui::hideBloodSugarErrorMessage, schedulersProvider.ui())
        .addAction(HideDateErrorMessage::class.java, ui::hideDateErrorMessage, schedulersProvider.ui())
        .addAction(Dismiss::class.java, ui::dismiss, schedulersProvider.ui())
        .addAction(ShowDateEntryScreen::class.java, ui::showDateEntryScreen, schedulersProvider.ui())
        .addConsumer(ShowBloodSugarValidationError::class.java, { showBloodSugarValidationError(it.result) }, schedulersProvider.ui())
        .addConsumer(ShowBloodSugarEntryScreen::class.java, { showBloodSugarEntryScreen(it.date) }, schedulersProvider.ui())
        .addTransformer(PrefillDate::class.java, prefillDate(schedulersProvider.ui()))
        .addConsumer(ShowDateValidationError::class.java, { showDateValidationError(it.result) }, schedulersProvider.ui())
        .addAction(SetBloodSugarSavedResultAndFinish::class.java, ui::setBloodSugarSavedResultAndFinish, schedulersProvider.ui())
        .build()
  }

  private fun showBloodSugarValidationError(result: BloodSugarValidator.Result) {
    when (result) {
      ErrorBloodSugarEmpty -> ui.showBloodSugarEmptyError()
      ErrorBloodSugarTooHigh -> ui.showBloodSugarHighError()
      ErrorBloodSugarTooLow -> ui.showBloodSugarLowError()
    }
  }

  private fun showBloodSugarEntryScreen(date: LocalDate) {
    with(ui) {
      showBloodSugarEntryScreen()
      showDateOnDateButton(date)
    }
  }

  private fun prefillDate(scheduler: Scheduler): ObservableTransformer<PrefillDate, BloodSugarEntryEvent> {
    return ObservableTransformer { prefillDates ->
      prefillDates
          .map { Instant.now(userClock).toLocalDateAtZone(userClock.zone) }
          .observeOn(scheduler)
          .doOnNext { setDateOnInputFields(it) }
          .doOnNext { ui.showDateOnDateButton(it) }
          .map { DatePrefilled(it) }
    }
  }

  private fun setDateOnInputFields(dateToSet: LocalDate) {
    ui.setDateOnInputFields(
        dateToSet.dayOfMonth.toString(),
        dateToSet.monthValue.toString(),
        getYear(dateToSet)
    )
  }

  private fun getYear(date: LocalDate): String =
      date.year.toString().substring(startIndex = 2, endIndex = 4)

  private fun showDateValidationError(result: UserInputDateValidator.Result) {
    when (result) {
      InvalidPattern -> ui.showInvalidDateError()
      DateIsInFuture -> ui.showDateIsInFutureError()
      is Valid -> throw IllegalStateException("Date validation error cannot be $result")
    }.exhaustive()
  }
}
