package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class CustomDrugEntryUpdateTest {

  @Test
  fun `when dosage is edited, then update the model with the new dosage`() {
    val updateSpec = UpdateSpec(CustomDrugEntryUpdate())
    val defaultModel = CustomDrugEntryModel.default()
    val dosage = "200 mg"

    updateSpec.given(defaultModel)
        .whenEvent(DosageEdited(dosage = dosage))
        .then(assertThatNext(
            hasModel(defaultModel.dosageEdited(dosage = dosage)),
            hasNoEffects()
        ))
  }
}
