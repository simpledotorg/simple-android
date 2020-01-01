package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarEmpty
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooHigh
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooLow
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.LocalDate

class BloodSugarEntryEffectHandler(
    private val ui: BloodSugarEntryUi,
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
}
