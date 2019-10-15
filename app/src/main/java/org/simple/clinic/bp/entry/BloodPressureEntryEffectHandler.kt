package org.simple.clinic.bp.entry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.LocalDate

object BloodPressureEntryEffectHandler {
  fun create(
      ui: BloodPressureEntryUi,
      inputDatePaddingCharacter: UserInputDatePaddingCharacter,
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<BloodPressureEntryEffect, BloodPressureEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodPressureEntryEffect, BloodPressureEntryEvent>()
        .addConsumer(PrefillDate::class.java, { prefillDate(ui, it.date, inputDatePaddingCharacter) }, schedulersProvider.ui())
        .build()
  }

  fun prefillDate(
      ui: BloodPressureEntryUi,
      date: LocalDate,
      inputDatePaddingCharacter: UserInputDatePaddingCharacter
  ) {
    val dayString = date.dayOfMonth.toString().padStart(length = 2, padChar = inputDatePaddingCharacter.value)
    val monthString = date.monthValue.toString().padStart(length = 2, padChar = inputDatePaddingCharacter.value)
    val yearString = date.year.toString().substring(startIndex = 2, endIndex = 4)
    ui.setDateOnInputFields(dayString, monthString, yearString)
    ui.showDateOnDateButton(date)
  }
}
