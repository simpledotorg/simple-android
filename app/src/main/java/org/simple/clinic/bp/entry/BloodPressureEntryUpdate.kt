package org.simple.clinic.bp.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class BloodPressureEntryUpdate : Update<BloodPressureEntryModel, BloodPressureEntryEvent, BloodPressureEntryEffect> {
  override fun update(
      model: BloodPressureEntryModel,
      event: BloodPressureEntryEvent
  ): Next<BloodPressureEntryModel, BloodPressureEntryEffect> {
    return when (event) {
      is SystolicChanged -> if (isSystolicValueComplete(event.systolic)) {
        next(model.withSystolic(event.systolic), HideBpErrorMessage, ChangeFocusToDiastolic)
      } else {
        next(model.withSystolic(event.systolic), HideBpErrorMessage as BloodPressureEntryEffect)
      }

      is DiastolicChanged -> next(model.withDiastolic(event.diastolic), HideBpErrorMessage)

      is DiastolicBackspaceClicked -> if (model.diastolic.isNotEmpty()) {
        next(model.deleteDiastolicLastDigit())
      } else {
        val updatedModel = model.deleteSystolicLastDigit()
        next(updatedModel, ChangeFocusToSystolic, SetSystolic(updatedModel.systolic))
      }

      is BloodPressureMeasurementFetched -> {
        val bloodPressureMeasurement = event.bloodPressureMeasurement
        val systolicString = bloodPressureMeasurement.systolic.toString()
        val diastolicString = bloodPressureMeasurement.diastolic.toString()

        val modelWithSystolicAndDiastolic = model
            .withSystolic(systolicString)
            .withDiastolic(diastolicString)

        next(modelWithSystolicAndDiastolic, SetSystolic(systolicString), SetDiastolic(diastolicString))
      }

      is RemoveClicked -> dispatch(
          ShowConfirmRemoveBloodPressureDialog((model.openAs as OpenAs.Update).bpUuid)
      )

      is ScreenChanged -> next(model.screenChanged(event.type))

      is BackPressed -> if (model.activeScreen == BP_ENTRY) {
        dispatch(Dismiss as BloodPressureEntryEffect)
      } else {
        noChange()
      }

      is DayChanged -> dispatch(HideDateErrorMessage)

      is MonthChanged -> dispatch(HideDateErrorMessage)

      is YearChanged -> dispatch(HideDateErrorMessage)

      else -> noChange()
    }
  }

  private fun isSystolicValueComplete(systolicText: String): Boolean {
    return (systolicText.length == 3 && systolicText.matches("^[123].*$".toRegex()))
        || (systolicText.length == 2 && systolicText.matches("^[789].*$".toRegex()))
  }
}
