package org.simple.clinic.textInputdatepicker

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.DismissSheet
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.HideDateErrorMessage
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.ShowDateValidationError
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Notvalid.DateIsInPast
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Notvalid.InvalidPattern
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Notvalid.MaximumAllowedDateRange
import org.simple.clinic.textInputdatepicker.TextInputDatePickerValidator.Result.Valid
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.scheduler.SchedulersProvider

class TextInputDatePickerEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: TextInputDatePickerUiActions
) {

  fun build(): ObservableTransformer<TextInputDatePickerEffect, TextInputDatePickerEvent> {
    return RxMobius
        .subtypeEffectHandler<TextInputDatePickerEffect, TextInputDatePickerEvent>()
        .addAction(DismissSheet::class.java, uiActions::dismissSheet, schedulersProvider.ui())
        .addAction(HideDateErrorMessage::class.java, uiActions::hideErrorMessage, schedulersProvider.ui())
        .addConsumer(ShowDateValidationError::class.java, ::showDateValidationErrors, schedulersProvider.ui())
        .build()
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
