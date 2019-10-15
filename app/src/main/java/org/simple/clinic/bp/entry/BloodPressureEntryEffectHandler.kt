package org.simple.clinic.bp.entry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateAtZone
import org.threeten.bp.Instant

object BloodPressureEntryEffectHandler {
  fun create(
      ui: BloodPressureEntryUi,
      userClock: UserClock,
      inputDatePaddingCharacter: UserInputDatePaddingCharacter,
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<BloodPressureEntryEffect, BloodPressureEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodPressureEntryEffect, BloodPressureEntryEvent>()
        .addAction(PrefillDateForNewEntry::class.java, { prefillDateForNewEntry(ui, userClock, inputDatePaddingCharacter) }, schedulersProvider.ui())
        .build()
  }

  private fun prefillDateForNewEntry(
      ui: BloodPressureEntryUi,
      userClock: UserClock,
      inputDatePaddingCharacter: UserInputDatePaddingCharacter
  ) {
    val date = Instant.now(userClock).toLocalDateAtZone(userClock.zone)
    val dayString = date.dayOfMonth.toString().padStart(length = 2, padChar = inputDatePaddingCharacter.value)
    val monthString = date.monthValue.toString().padStart(length = 2, padChar = inputDatePaddingCharacter.value)
    val yearString = date.year.toString().substring(startIndex = 2, endIndex = 4)
    ui.setDateOnInputFields(dayString, monthString, yearString)
    ui.showDateOnDateButton(date)
  }
}
