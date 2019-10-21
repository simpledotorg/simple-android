package org.simple.clinic.bp.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.BP_ENTRY
import org.simple.clinic.bp.entry.BloodPressureEntrySheet.ScreenType.DATE_ENTRY
import org.simple.clinic.bp.entry.BpValidator.Validation.Success
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
import org.threeten.bp.LocalDate

class BloodPressureEntryUpdate(
    private val bpValidator: BpValidator,
    private val dateValidator: UserInputDateValidator,
    private val dateInUserTimeZone: LocalDate,
    private val inputDatePaddingCharacter: UserInputDatePaddingCharacter
) : Update<BloodPressureEntryModel, BloodPressureEntryEvent, BloodPressureEntryEffect> {
  override fun update(
      model: BloodPressureEntryModel,
      event: BloodPressureEntryEvent
  ): Next<BloodPressureEntryModel, BloodPressureEntryEffect> {
    return when (event) {
      is SystolicChanged -> if (isSystolicValueComplete(event.systolic)) {
        next(model.systolicChanged(event.systolic), HideBpErrorMessage, ChangeFocusToDiastolic)
      } else {
        next(model.systolicChanged(event.systolic), HideBpErrorMessage as BloodPressureEntryEffect)
      }

      is DiastolicChanged -> next(model.diastolicChanged(event.diastolic), HideBpErrorMessage)

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
            .systolicChanged(systolicString)
            .diastolicChanged(diastolicString)

        next(
            modelWithSystolicAndDiastolic,
            SetSystolic(systolicString),
            SetDiastolic(diastolicString),
            PrefillDate.forUpdateEntry(bloodPressureMeasurement.recordedAt)
        )
      }

      is RemoveClicked -> dispatch(
          ShowConfirmRemoveBloodPressureDialog((model.openAs as OpenAs.Update).bpUuid)
      )

      is ScreenChanged -> next(model.screenChanged(event.type))

      is BackPressed -> if (model.activeScreen == BP_ENTRY) {
        dispatch(Dismiss as BloodPressureEntryEffect)
      } else if (model.activeScreen == DATE_ENTRY) {
        val dateText = formatToPaddedDate(model.day, model.month, model.twoDigitYear, model.year)
        val result = dateValidator.validate(dateText, dateInUserTimeZone)
        if (result is Valid) {
          dispatch(ShowBpEntryScreen(result.parsedDate) as BloodPressureEntryEffect)
        } else {
          dispatch(ShowDateValidationError(result) as BloodPressureEntryEffect)
        }
      } else {
        noChange()
      }

      is DayChanged -> next(
          model.dayChanged(event.day),
          HideDateErrorMessage
      )

      is MonthChanged -> next(
          model.monthChanged(event.month),
          HideDateErrorMessage
      )

      is YearChanged -> next(
          model.yearChanged(event.twoDigitYear),
          HideDateErrorMessage
      )

      is BloodPressureDateClicked -> {
        val result = bpValidator.validate(model.systolic, model.diastolic)
        return if (result is Success) {
          dispatch(ShowDateEntryScreen)
        } else {
          dispatch(ShowBpValidationError(result))
        }
      }

      is SaveClicked -> {
        val result = bpValidator.validate(model.systolic, model.diastolic)
        return if (result !is Success) {
          dispatch(ShowBpValidationError(result))
        } else {
          noChange()
        }
      }

      is ShowBpClicked -> {
        val dateText = formatToPaddedDate(model.day, model.month, model.twoDigitYear, model.year)
        val result = dateValidator.validate(dateText, dateInUserTimeZone)
        if (result is Valid) {
          dispatch(ShowBpEntryScreen(result.parsedDate) as BloodPressureEntryEffect)
        } else {
          dispatch(ShowDateValidationError(result) as BloodPressureEntryEffect)
        }
      }

      else -> noChange()
    }
  }

  private fun isSystolicValueComplete(systolicText: String): Boolean {
    return (systolicText.length == 3 && systolicText.matches("^[123].*$".toRegex()))
        || (systolicText.length == 2 && systolicText.matches("^[789].*$".toRegex()))
  }

  private fun formatToPaddedDate(day: String, month: String, twoDigitYear: String, fourDigitYear: String): String {
    val paddedDd = day.padStart(length = 2, padChar = inputDatePaddingCharacter.value)
    val paddedMm = month.padStart(length = 2, padChar = inputDatePaddingCharacter.value)
    val paddedYy = twoDigitYear.padStart(length = 2, padChar = inputDatePaddingCharacter.value)

    val firstTwoDigitsOfYear = fourDigitYear.substring(0, 2)
    val paddedYyyy = firstTwoDigitsOfYear + paddedYy
    return "$paddedDd/$paddedMm/$paddedYyyy"
  }
}
