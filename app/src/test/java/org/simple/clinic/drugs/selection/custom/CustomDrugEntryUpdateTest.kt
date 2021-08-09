package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.drugs.search.DrugFrequency

class CustomDrugEntryUpdateTest {

  private val updateSpec = UpdateSpec(CustomDrugEntryUpdate())
  private val defaultModel = CustomDrugEntryModel.default()

  @Test
  fun `when dosage is edited, then update the model with the new dosage and update the sheet title`() {
    val dosage = "200 mg"

    updateSpec.given(defaultModel)
        .whenEvent(DosageEdited(dosage = dosage))
        .then(assertThatNext(
            hasModel(defaultModel.dosageEdited(dosage = dosage)),
            hasEffects(SetSheetTitle(drugName, dosage, null))
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

  @Test
  fun `when edit frequency is clicked, then show edit frequency dialog`() {
    val frequency = DrugFrequency.OD

    updateSpec.given(defaultModel)
        .whenEvent(EditFrequencyClicked(frequency))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowEditFrequencyDialog(frequency))
        ))
  }

  @Test
  fun `when frequency is edited, then update the model and set drug frequency in the ui`() {
    val frequency = DrugFrequency.OD
    updateSpec.given(defaultModel)
        .whenEvent(FrequencyEdited(frequency))
        .then(assertThatNext(
            hasModel(defaultModel.frequencyEdited(frequency)),
            hasEffects(SetDrugFrequency(frequency))
        ))
  }
}
