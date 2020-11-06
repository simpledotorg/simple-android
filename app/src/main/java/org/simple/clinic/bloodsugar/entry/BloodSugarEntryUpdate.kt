package org.simple.clinic.bloodsugar.entry

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType.BLOOD_SUGAR_ENTRY
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType.DATE_ENTRY
import org.simple.clinic.bloodsugar.entry.BloodSugarSaveState.NOT_SAVING_BLOOD_SUGAR
import org.simple.clinic.bloodsugar.entry.BloodSugarSaveState.SAVING_BLOOD_SUGAR
import org.simple.clinic.bloodsugar.entry.ValidationResult.Valid
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result
import java.time.LocalDate

class BloodSugarEntryUpdate @AssistedInject constructor(
    private val dateValidator: UserInputDateValidator,
    @Assisted private val dateInUserTimeZone: LocalDate,
    private val inputDatePaddingCharacter: UserInputDatePaddingCharacter,
    private val bloodSugarUnitPreference: Preference<BloodSugarUnitPreference>
) : Update<BloodSugarEntryModel, BloodSugarEntryEvent, BloodSugarEntryEffect> {
  @AssistedInject.Factory
  interface Factory {
    fun create(dateInUserTimeZone: LocalDate): BloodSugarEntryUpdate
  }

  override fun update(
      model: BloodSugarEntryModel,
      event: BloodSugarEntryEvent
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    return when (event) {
      is ScreenChanged -> next(model.screenChanged(event.type))
      is DatePrefilled -> next(model.datePrefilled(event.prefilledDate))
      is BloodSugarMeasurementFetched -> bloodSugarMeasurementFetched(model, event)
      is BloodSugarChanged -> next(model.bloodSugarChanged(event.bloodSugarReading), HideBloodSugarErrorMessage)
      is DayChanged -> onDateChanged(model.dayChanged(event.day))
      is MonthChanged -> onDateChanged(model.monthChanged(event.month))
      is YearChanged -> onDateChanged(model.yearChanged(event.fourDigitYear))
      BackPressed -> onBackPressed(model)
      BloodSugarDateClicked -> onBloodSugarDateClicked(model)
      ShowBloodSugarEntryClicked -> showBloodSugarClicked(model)
      SaveClicked -> onSaveClicked(model)
      is BloodSugarSaved -> next(model.bloodSugarStateChanged(NOT_SAVING_BLOOD_SUGAR), SetBloodSugarSavedResultAndFinish)
      RemoveBloodSugarClicked -> dispatch(ShowConfirmRemoveBloodSugarDialog((model.openAs as OpenAs.Update).bloodSugarMeasurementUuid))
      is BloodSugarUnitPreferenceLoaded -> noChange()
    }
  }

  private fun bloodSugarMeasurementFetched(
      model: BloodSugarEntryModel,
      event: BloodSugarMeasurementFetched
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    val bloodSugarMeasurement = event.bloodSugarMeasurement
    val recordedAt = bloodSugarMeasurement.recordedAt
    val bloodSugarReading = bloodSugarMeasurement.reading.displayValue(bloodSugarUnitPreference.get())
    val modelWithBloodSugarChanged = model.bloodSugarChanged(bloodSugarReading)

    return next(
        modelWithBloodSugarChanged,
        SetBloodSugarReading(bloodSugarReading),
        PrefillDate.forUpdateEntry(recordedAt)
    )
  }

  private fun onSaveClicked(
      model: BloodSugarEntryModel
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    return if (model.bloodSugarSaveState == SAVING_BLOOD_SUGAR) {
      noChange()
    } else {
      val bloodSugarValidationResult = model.bloodSugarReading.validate()
      val dateValidationResult = dateValidator.validate(getDateText(model), dateInUserTimeZone)
      val validationErrorEffects = getValidationErrorEffects(bloodSugarValidationResult, dateValidationResult)

      if (validationErrorEffects.isNotEmpty()) {
        Next.dispatch(validationErrorEffects)
      } else {
        next(model.bloodSugarStateChanged(SAVING_BLOOD_SUGAR), getCreateorUpdateEntryEffect(model, dateValidationResult))
      }
    }
  }

  private fun getValidationErrorEffects(
      bloodSugarValidationResult: ValidationResult,
      dateValidationResult: Result
  ): Set<BloodSugarEntryEffect> {
    val validationErrorEffects = mutableSetOf<BloodSugarEntryEffect>()

    if (bloodSugarValidationResult !is Valid) {
      validationErrorEffects.add(ShowBloodSugarValidationError(bloodSugarValidationResult))
    }

    if (dateValidationResult !is Result.Valid) {
      validationErrorEffects.add(ShowDateValidationError(dateValidationResult))
    }
    return validationErrorEffects.toSet()
  }

  private fun getCreateorUpdateEntryEffect(
      model: BloodSugarEntryModel,
      dateValidationResult: Result
  ): BloodSugarEntryEffect {
    val userEnteredDate = (dateValidationResult as Result.Valid).parsedDate
    val prefillDate = model.prefilledDate!!

    return when (val openAs = model.openAs) {
      is OpenAs.New -> CreateNewBloodSugarEntry(
          openAs.patientId,
          userEnteredDate,
          prefillDate,
          model.bloodSugarReading
      )
      is OpenAs.Update -> UpdateBloodSugarEntry(
          openAs.bloodSugarMeasurementUuid,
          userEnteredDate,
          prefillDate,
          model.bloodSugarReading
      )
    }
  }

  private fun showBloodSugarClicked(
      model: BloodSugarEntryModel
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    val result = dateValidator.validate(getDateText(model), dateInUserTimeZone)
    val effect = if (result is Result.Valid) {
      ShowBloodSugarEntryScreen(result.parsedDate)
    } else {
      ShowDateValidationError(result)
    }
    return dispatch(effect)
  }

  private fun onBloodSugarDateClicked(
      model: BloodSugarEntryModel
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    val result = model.bloodSugarReading.validate()
    val effect = if (result is Valid) {
      ShowDateEntryScreen
    } else {
      ShowBloodSugarValidationError(result)
    }
    return dispatch(effect)
  }

  private fun onBackPressed(
      model: BloodSugarEntryModel
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> {
    return when (model.activeScreen) {
      BLOOD_SUGAR_ENTRY -> dispatch(Dismiss as BloodSugarEntryEffect)
      DATE_ENTRY -> showBloodSugarClicked(model)
    }
  }

  private fun onDateChanged(
      updatedModel: BloodSugarEntryModel
  ): Next<BloodSugarEntryModel, BloodSugarEntryEffect> =
      next(updatedModel, HideDateErrorMessage)

  private fun getDateText(model: BloodSugarEntryModel) =
      formatToPaddedDate(model.day, model.month, model.fourDigitYear)

  private fun formatToPaddedDate(day: String, month: String, fourDigitYear: String): String {
    val paddedDd = day.padStart(length = 2, padChar = inputDatePaddingCharacter.value)
    val paddedMm = month.padStart(length = 2, padChar = inputDatePaddingCharacter.value)

    return "$paddedDd/$paddedMm/$fourDigitYear"
  }
}
