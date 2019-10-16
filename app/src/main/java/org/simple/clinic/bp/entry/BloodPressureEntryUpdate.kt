package org.simple.clinic.bp.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.justEffect
import org.simple.clinic.mobius.justEffects

class BloodPressureEntryUpdate : Update<BloodPressureEntryModel, BloodPressureEntryEvent, BloodPressureEntryEffect> {
  override fun update(
      model: BloodPressureEntryModel,
      event: BloodPressureEntryEvent
  ): Next<BloodPressureEntryModel, BloodPressureEntryEffect> {
    return when (event) {
      is SystolicChanged -> if (isSystolicValueComplete(event.systolic)) {
        justEffects(HideBpErrorMessage, ChangeFocusToDiastolic)
      } else {
        justEffect<BloodPressureEntryModel, BloodPressureEntryEffect>(HideBpErrorMessage)
      }

      else -> noChange()
    }
  }

  private fun isSystolicValueComplete(systolicText: String): Boolean {
    return (systolicText.length == 3 && systolicText.matches("^[123].*$".toRegex()))
        || (systolicText.length == 2 && systolicText.matches("^[789].*$".toRegex()))
  }
}
