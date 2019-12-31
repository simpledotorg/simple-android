package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class BloodSugarEntryUpdateTest {

  private val bloodSugarValidator = BloodSugarValidator()

  private val validBloodSugar = "30"
  private val defaultModel = BloodSugarEntryModel.BLANK
  private val updateSpec = UpdateSpec<BloodSugarEntryModel, BloodSugarEntryEvent, BloodSugarEntryEffect>(
      BloodSugarEntryUpdate(
          bloodSugarValidator
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
    updateSpec
        .given(defaultModel)
        .whenEvents(DayChanged, MonthChanged, YearChanged)
        .then(assertThatNext(
            hasModel(
                defaultModel
                    .dayChanged()
                    .monthChanged()
                    .yearChanged()
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
            hasEffects(ShowBloodSugarValidationError as BloodSugarEntryEffect)
        ))
  }
}
