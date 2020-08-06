package org.simple.clinic.bp.entry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.bp.BloodPressureReading
import org.simple.clinic.bp.ValidationResult.ErrorDiastolicEmpty
import org.simple.clinic.bp.ValidationResult.ErrorSystolicEmpty
import org.simple.clinic.bp.entry.BloodPressureSaveState.SAVING_BLOOD_PRESSURE
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class BloodPressureEntryUpdateTest {

  private val patientUuid = UUID.fromString("6fc16e72-39a5-4568-86db-1f8b1c0c08d3")
  private val defaultModel = BloodPressureEntryModel.create(OpenAs.New(patientUuid), 2018)

  private val clock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private val update = BloodPressureEntryUpdate(
      UserInputDateValidator(clock, dateFormatter),
      LocalDate.now(clock),
      UserInputDatePaddingCharacter.ZERO
  )

  private val spec = UpdateSpec(update)

  @Test
  fun `when the systolic entry is blank, the error message must be shown when clicking save`() {
    val model = defaultModel
        .systolicChanged("")
        .diastolicChanged("80")

    spec
        .given(model)
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBpValidationError(ErrorSystolicEmpty) as BloodPressureEntryEffect)
        ))
  }

  @Test
  fun `when the diastolic entry is blank, the error message must be shown when clicking save`() {
    val model = defaultModel
        .systolicChanged("120")
        .diastolicChanged("")

    spec
        .given(model)
        .whenEvent(SaveClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBpValidationError(ErrorDiastolicEmpty) as BloodPressureEntryEffect)
        ))
  }

  @Test
  fun `when the entered date is invalid, an error message must be shown`() {
    val model = defaultModel
        .dayChanged("32")
        .yearChanged("9021")
        .monthChanged("3")
    spec
        .given(model)
        .whenEvents(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(
                    ShowDateValidationError(Invalid.InvalidPattern) as BloodPressureEntryEffect
                )
            )
        )
  }

  @Test
  fun `when the entered date is a future date, an error message must be shown`() {
    val model = defaultModel
        .dayChanged("12")
        .yearChanged("2039")
        .monthChanged("3")
    spec
        .given(model)
        .whenEvents(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(
                    ShowDateValidationError(Invalid.DateIsInFuture) as BloodPressureEntryEffect
                )
            )
        )
  }

  @Test
  fun `when the save button is clicked and it has validation errors, then error messages must be shown`() {
    val model = defaultModel
    spec
        .given(model)
        .whenEvents(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(
                    ShowDateValidationError(Invalid.InvalidPattern) as BloodPressureEntryEffect,
                    ShowBpValidationError(ErrorSystolicEmpty) as BloodPressureEntryEffect
                )
            )
        )
  }

  @Test
  fun `when blood pressure is saved, then close the sheet`() {
    spec
        .given(defaultModel)
        .whenEvents(BloodPressureSaved(wasDateChanged = false))
        .then(
            assertThatNext(
                hasModel(defaultModel.bloodPressureStateChanged(BloodPressureSaveState.NOT_SAVING_BLOOD_PRESSURE)),
                hasEffects(SetBpSavedResultAndFinish as BloodPressureEntryEffect)
            )
        )
  }

  @Test
  fun `when the save button is clicked and saving blood pressure operation is on-going, then do nothing`() {
    val model = defaultModel.bloodPressureStateChanged(SAVING_BLOOD_PRESSURE)

    spec
        .given(model)
        .whenEvents(SaveClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when save is clicked and there are no validation errors, then save the blood pressure`() {
    val reading = BloodPressureReading(110, 90)
    val entryDate = LocalDate.parse("2018-01-01")
    val model = defaultModel
        .systolicChanged(reading.systolic.toString())
        .diastolicChanged(reading.diastolic.toString())
        .yearChanged(entryDate.year.toString())
        .monthChanged(entryDate.month.value.toString())
        .dayChanged(entryDate.dayOfMonth.toString())
        .datePrefilled(entryDate)

    spec
        .given(model)
        .whenEvents(SaveClicked)
        .then(
            assertThatNext(
                hasModel(model.bloodPressureStateChanged(SAVING_BLOOD_PRESSURE)),
                hasEffects(
                    CreateNewBpEntry(patientUuid = patientUuid, reading = reading, userEnteredDate = entryDate, prefilledDate = entryDate)
                        as BloodPressureEntryEffect
                )
            )
        )
  }
}
