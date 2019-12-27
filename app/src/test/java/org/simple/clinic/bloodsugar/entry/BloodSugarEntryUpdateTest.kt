package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class BloodSugarEntryUpdateTest {

  private val defaultModel = BloodSugarEntryModel.BLANK
  private val updateSpec = UpdateSpec<BloodSugarEntryModel, BloodSugarEntryEvent, BloodSugarEntryEffect>(BloodSugarEntryUpdate())

  @Test
  fun `when blood sugar value changes, hide any blood sugar error message`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(BloodSugarChanged)
        .then(assertThatNext(
            hasModel(defaultModel.bloodSugarChanged()),
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
}
