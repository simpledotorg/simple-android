package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class CustomDrugEntryUpdateTest {

  private val updateSpec = UpdateSpec(CustomDrugEntryUpdate())
  private val defaultModel = CustomDrugEntryModel.default()

  @Test
  fun `when dosage is edited, then update the model with the new dosage`() {
    val dosage = "200 mg"

    updateSpec.given(defaultModel)
        .whenEvent(DosageEdited(dosage = dosage))
        .then(assertThatNext(
            hasModel(defaultModel.dosageEdited(dosage = dosage)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when dosage edit text focus is changed, then update the model`() {
    val hasFocus = true

    updateSpec.given(defaultModel)
        .whenEvent(DosageFocusChanged(hasFocus))
        .then(
            assertThatNext(
                hasModel(defaultModel.dosageFocusChanged(hasFocus)),
                hasNoEffects()
            )
        )
  }
}
