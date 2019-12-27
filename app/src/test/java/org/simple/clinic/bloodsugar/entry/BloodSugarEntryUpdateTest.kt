package org.simple.clinic.bloodsugar.entry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class BloodSugarEntryUpdateTest {

  @Test
  fun `when blood sugar value changes, hide any blood sugar error message`() {
    val defaultModel = BloodSugarEntryModel.BLANK
    val updateSpec = UpdateSpec<BloodSugarEntryModel, BloodSugarEntryEvent, BloodSugarEntryEffect>(BloodSugarEntryUpdate())

    updateSpec
        .given(defaultModel)
        .whenEvent(BloodSugarChanged)
        .then(assertThatNext(
            hasModel(defaultModel.bloodSugarChanged()),
            hasEffects(HideBloodSugarErrorMessage as BloodSugarEntryEffect)
        ))
  }
}
