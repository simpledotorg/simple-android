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
  private val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugName(patientUuid, drugName))

  @Test
  fun `when dosage is edited, then update the model with the new dosage and update the sheet title`() {
    val dosage = "200 mg"
    val drugNameLoadedModel = defaultModel.drugNameLoaded(drugName)

    updateSpec.given(drugNameLoadedModel)
        .whenEvent(DosageEdited(dosage = dosage))
        .then(assertThatNext(
            hasModel(drugNameLoadedModel.dosageEdited(dosage = dosage)),
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
  fun `when frequency is edited, then update the model and set drug frequency and update the sheet title in the ui`() {
    val frequency = DrugFrequency.OD
    val drugNameLoadedModel = defaultModel.drugNameLoaded(drugName)

    updateSpec.given(drugNameLoadedModel)
        .whenEvent(FrequencyEdited(frequency))
        .then(assertThatNext(
            hasModel(drugNameLoadedModel.frequencyEdited(frequency)),
            hasEffects(SetDrugFrequency(frequency), SetSheetTitle(drugName, null, frequency))
        ))
  }

  @Test
  fun `when add button is clicked and the sheet is opened in create mode from drug list with edited dosage and frequency values, then add the drug to the custom drug list`() {
    val dosage = "200 mg"
    val frequency = DrugFrequency.OD
    val drug = TestData.drug(id = UUID.fromString("6106544f-2b18-410d-992b-81860a08f02a"), name = drugName)
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugList(patientUuid, drug)).drugNameLoaded(drugName)

    updateSpec
        .given(defaultModel.dosageEdited(dosage).frequencyEdited(frequency))
        .whenEvent(AddMedicineButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveCustomDrugToPrescription(patientUuid, drugName, dosage, drug.rxNormCode, frequency))
        ))
  }
}
