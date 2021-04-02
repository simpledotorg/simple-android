package org.simple.clinic.textInputdatepicker

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.DismissSheet

class TextInputDatePickerUpdateTest {

  private val defaultModel = TextInputDatePickerModel.create()

  @Test
  fun `when close button is clicked, then close the sheet`() {
    UpdateSpec(TextInputDatePickerUpdate())
        .given(defaultModel)
        .whenEvent(DismissSheetClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(DismissSheet)
            )
        )
  }
}
