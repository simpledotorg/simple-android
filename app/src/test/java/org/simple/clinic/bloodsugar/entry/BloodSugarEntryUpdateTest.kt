package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooHigh
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class BloodSugarEntryUpdateTest {

  private val bloodSugarValidator = BloodSugarValidator()
  private val testUserClock = TestUserClock()
  private val dateValidator = UserInputDateValidator(testUserClock, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))

  private val validBloodSugar = "30"
  private val patientUuid = UUID.fromString("79145baf-7a5c-4442-ab30-2da564a32944")

  private val defaultModel = BloodSugarEntryModel.create(LocalDate.now(testUserClock).year, New(patientUuid, Random))
  private val updateSpec = UpdateSpec<BloodSugarEntryModel, BloodSugarEntryEvent, BloodSugarEntryEffect>(
      BloodSugarEntryUpdate(
          bloodSugarValidator,
          dateValidator,
          LocalDate.now(testUserClock.zone),
          UserInputDatePaddingCharacter.ZERO
      )
  )

  @Test
  fun `when blood sugar value changes, hide any blood sugar error message`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(BloodSugarChanged(validBloodSugar))
        .then(assertThatNext(
            hasModel(defaultModel.bloodSugarChanged(validBloodSugar)),
            hasEffects(HideBloodSugarErrorMessage as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when date values change, hide any date error message`() {
    val day = "14"
    val month = "02"
    val year = "94"

    updateSpec
        .given(defaultModel)
        .whenEvents(
            DayChanged(day),
            MonthChanged(month),
            YearChanged(year)
        )
        .then(assertThatNext(
            hasModel(
                defaultModel
                    .dayChanged(day)
                    .monthChanged(month)
                    .yearChanged(year)
            ),
            hasEffects(HideDateErrorMessage as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when blood sugar entry is active and back is pressed, then the sheet should be dismissed`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(BackPressed)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(Dismiss as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when blood sugar entry is active and value is valid and date button is pressed, then date entry should be shown`() {
    updateSpec
        .given(defaultModel.bloodSugarChanged(validBloodSugar))
        .whenEvent(BloodSugarDateClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowDateEntryScreen as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when blood sugar entry is active and value is invalid and date button is pressed, then show blood sugar validation errors`() {
    val invalidBloodSugar = "1001"

    updateSpec
        .given(defaultModel.bloodSugarChanged(invalidBloodSugar))
        .whenEvent(BloodSugarDateClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBloodSugarValidationError(ErrorBloodSugarTooHigh) as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when date entry screen is active and has valid date and show blood sugar entry is pressed, then show blood sugar entry sheet`() {
    val bloodSugarDate = LocalDate.of(1994, 2, 14)
    updateSpec
        .given(
            defaultModel
                .dayChanged(bloodSugarDate.dayOfMonth.toString())
                .monthChanged(bloodSugarDate.monthValue.toString())
                .yearChanged(bloodSugarDate.year.toString().substring(2))
        )
        .whenEvent(ShowBloodSugarEntryClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBloodSugarEntryScreen(bloodSugarDate) as BloodSugarEntryEffect)
        ))
  }

  @Test
  fun `when blood sugar sheet has invalid date and show blood sugar entry is pressed, then show date validation errors`() {
    val day = "14"
    val month = "13"
    val year = "94"

    updateSpec
        .given(
            defaultModel
                .dayChanged(day)
                .monthChanged(month)
                .yearChanged(year)
        )
        .whenEvent(ShowBloodSugarEntryClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowDateValidationError(InvalidPattern) as BloodSugarEntryEffect)
        ))
  }
}
