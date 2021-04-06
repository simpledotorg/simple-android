package org.simple.clinic.textInputdatepicker

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.DismissSheet
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.HideDateErrorMessage
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.PrefilledDate
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.ShowDateValidationError
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.UserEnteredDateSelected
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Notvalid.DateIsInPast
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Notvalid.InvalidPattern
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Notvalid.MaximumAllowedDateRange
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Valid
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateAtZone
import java.time.Instant
import java.time.LocalDate

class TextInputDatePickerEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: TextInputDatePickerUiActions,
    private val userClock: UserClock
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: TextInputDatePickerUiActions): TextInputDatePickerEffectHandler
  }

  fun build(): ObservableTransformer<TextInputDatePickerEffect, TextInputDatePickerEvent> {
    return RxMobius
        .subtypeEffectHandler<TextInputDatePickerEffect, TextInputDatePickerEvent>()
        .addAction(DismissSheet::class.java, uiActions::dismissSheet, schedulersProvider.ui())
        .addAction(HideDateErrorMessage::class.java, uiActions::hideErrorMessage, schedulersProvider.ui())
        .addConsumer(ShowDateValidationError::class.java, ::showDateValidationErrors, schedulersProvider.ui())
        .addConsumer(UserEnteredDateSelected::class.java, { uiActions.userEnteredDateSelected(it.userEnteredDate) }, schedulersProvider.ui())
        .addTransformer(PrefilledDate::class.java, prefilledDate(schedulersProvider.ui()))
        .build()
  }

  private fun prefilledDate(scheduler: Scheduler): ObservableTransformer<PrefilledDate, TextInputDatePickerEvent> {
    return ObservableTransformer { prefilledDate ->
      prefilledDate
          .map(::getCurrentDateOrPrefilledDate)
          .observeOn(scheduler)
          .doOnNext(uiActions::setDateOnInputFields)
          .map { DatePrefilled(it) }
    }
  }

  private fun getCurrentDateOrPrefilledDate(prefillDate: PrefilledDate?): LocalDate {
    return if (prefillDate?.date != null) {
      prefillDate.date
    } else {
      val nowDate = Instant.now(userClock)
      nowDate.toLocalDateAtZone(userClock.zone)
    }
  }

  private fun showDateValidationErrors(effect: ShowDateValidationError) {
    when (val dateValidation = effect.dateValidation) {
      is InvalidPattern -> uiActions.showInvalidDateError()
      is DateIsInPast -> uiActions.showDateIsInPastError()
      is MaximumAllowedDateRange -> uiActions.showMaximumDateRangeError()
      is Valid -> throw IllegalStateException("Date validation error cannot be $dateValidation")
    }.exhaustive()
  }
}
