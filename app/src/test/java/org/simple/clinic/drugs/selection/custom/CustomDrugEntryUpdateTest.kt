package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.search.DrugFrequency
import java.util.UUID

class CustomDrugEntryUpdateTest {

  private val updateSpec = UpdateSpec(CustomDrugEntryUpdate())
  private val drugName = "Amlodipine"
  private val patientUuid = UUID.fromString("77f1d870-5c60-49f7-a4e2-2f1d60e4218c")
  private val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New(patientUuid), drug = null, drugName = drugName)

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
  fun `when frequency is edited, then update the model`() {
    val frequency = DrugFrequency.OD
    updateSpec.given(defaultModel)
        .whenEvent(FrequencyEdited(frequency))
        .then(assertThatNext(
            hasModel(defaultModel.frequencyEdited(frequency)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when add button is clicked, then add the drug to the custom drug list `() {
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New(patientUuid), drug = null, drugName = drugName)

    updateSpec
        .given(defaultModel)
        .whenEvent(AddMedicineButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveCustomDrugToPrescription(patientUuid, drugName, null, null, DrugFrequency.Unknown("None")))
        ))
  }

  @Test
  fun `when add button is clicked with a new dosage and frequency, then add the drug to the custom drug list with the new dosage and frequency `() {
    val dosage = "200 mg"
    val frequency = DrugFrequency.OD
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New(patientUuid), drug = null, drugName = drugName)

    updateSpec
        .given(defaultModel.dosageEdited(dosage).frequencyEdited(frequency))
        .whenEvent(AddMedicineButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveCustomDrugToPrescription(patientUuid, drugName, dosage, null, frequency))
        ))
  }

  @Test
  fun `when the new drug is added to the list, then close the bottom sheet`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CustomDrugSaved)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseBottomSheet)
        ))
  }
}
