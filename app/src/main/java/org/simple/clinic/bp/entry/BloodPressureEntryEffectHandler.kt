package org.simple.clinic.bp.entry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorDiastolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicEmpty
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicLessThanDiastolic
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooHigh
import org.simple.clinic.bp.entry.BpValidator.Validation.ErrorSystolicTooLow
import org.simple.clinic.bp.entry.BpValidator.Validation.Success
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateAtZone
import org.threeten.bp.Instant

object BloodPressureEntryEffectHandler {
  fun create(
      ui: BloodPressureEntryUi,
      userClock: UserClock,
      inputDatePaddingCharacter: UserInputDatePaddingCharacter,
      bloodPressureRepository: BloodPressureRepository,
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<BloodPressureEntryEffect, BloodPressureEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodPressureEntryEffect, BloodPressureEntryEvent>()
        .addConsumer(PrefillDate::class.java, { prefillDate(ui, it.date, userClock, inputDatePaddingCharacter) }, schedulersProvider.ui())
        .addAction(HideBpErrorMessage::class.java, ui::hideBpErrorMessage, schedulersProvider.ui())
        .addAction(ChangeFocusToDiastolic::class.java, ui::changeFocusToDiastolic, schedulersProvider.ui())
        .addAction(ChangeFocusToSystolic::class.java, ui::changeFocusToSystolic, schedulersProvider.ui())
        .addConsumer(SetSystolic::class.java, { ui.setSystolic(it.systolic) }, schedulersProvider.ui())
        .addTransformer(FetchBloodPressureMeasurement::class.java, fetchBloodPressureMeasurement(bloodPressureRepository, schedulersProvider.io()))
        .addConsumer(SetDiastolic::class.java, { ui.setDiastolic(it.diastolic) }, schedulersProvider.ui())
        .addConsumer(ShowConfirmRemoveBloodPressureDialog::class.java, { ui.showConfirmRemoveBloodPressureDialog(it.bpUuid) }, schedulersProvider.ui())
        .addAction(Dismiss::class.java, ui::dismiss, schedulersProvider.ui())
        .addAction(HideDateErrorMessage::class.java, ui::hideDateErrorMessage, schedulersProvider.ui())
        .addConsumer(ShowBpValidationError::class.java, { showBpValidationError(ui, it) }, schedulersProvider.ui())
        .addAction(ShowDateEntryScreen::class.java, ui::showDateEntryScreen, schedulersProvider.ui())
        .build()
  }

  private fun prefillDate(
      ui: BloodPressureEntryUi,
      instant: Instant?,
      userClock: UserClock,
      paddingCharacter: UserInputDatePaddingCharacter
  ) {
    val prefillInstant = instant ?: Instant.now(userClock)
    val date = prefillInstant.toLocalDateAtZone(userClock.zone)
    val dayString = date.dayOfMonth.toString().padStart(length = 2, padChar = paddingCharacter.value)
    val monthString = date.monthValue.toString().padStart(length = 2, padChar = paddingCharacter.value)
    val yearString = date.year.toString().substring(startIndex = 2, endIndex = 4)
    ui.setDateOnInputFields(dayString, monthString, yearString)
    ui.showDateOnDateButton(date)
  }

  private fun fetchBloodPressureMeasurement(
      bloodPressureRepository: BloodPressureRepository,
      scheduler: Scheduler
  ): ObservableTransformer<FetchBloodPressureMeasurement, BloodPressureEntryEvent> {
    return ObservableTransformer { bloodPressureMeasurements ->
      bloodPressureMeasurements
          .flatMap { bloodPressureRepository.measurement(it.bpUuid).subscribeOn(scheduler).take(1) }
          .map(::BloodPressureMeasurementFetched)
    }
  }

  private fun showBpValidationError(
      ui: BloodPressureEntryUi,
      validationError: ShowBpValidationError
  ) {
    when (validationError.result) {
      is ErrorSystolicLessThanDiastolic -> ui.showSystolicLessThanDiastolicError()
      is ErrorSystolicTooHigh -> ui.showSystolicHighError()
      is ErrorSystolicTooLow -> ui.showSystolicLowError()
      is ErrorDiastolicTooHigh -> ui.showDiastolicHighError()
      is ErrorDiastolicTooLow -> ui.showDiastolicLowError()
      is ErrorSystolicEmpty -> ui.showSystolicEmptyError()
      is ErrorDiastolicEmpty -> ui.showDiastolicEmptyError()
      is Success -> { /* Nothing to do here. */ }
    }.exhaustive()
  }
}
