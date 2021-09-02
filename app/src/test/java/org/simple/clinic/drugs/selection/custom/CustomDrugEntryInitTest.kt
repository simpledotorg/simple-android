package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class CustomDrugEntryInitTest {
  private val initSpec = InitSpec(CustomDrugEntryInit())
  private val drugName = "Amlodipine"
  private val dosagePlaceholder = "mg"

  @Test
  fun `when sheet is created in create mode from the drug list, fetch drug from the drugUuid, load drug frequency choice items and set the screen in progress state`() {
    val drugUuid = UUID.fromString("6106544f-2b18-410d-992b-81860a08f02a")
    val model = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugList(drugUuid), dosagePlaceholder)

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model.customDrugEntryProgressStateLoading()),
                hasEffects(FetchDrug(drugUuid), LoadDrugFrequencyChoiceItems)
            )
        )
  }

  @Test
  fun `when sheet is created in create mode from a drug name, then update the drug name in the model, load drug frequency choice items and set the progress state to done`() {
    val model = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugName(drugName), dosagePlaceholder)

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model.drugNameLoaded(drugName).customDrugEntryProgressStateLoaded()),
                hasEffects(LoadDrugFrequencyChoiceItems)
            )
        )
  }

  @Test
  fun `when sheet is created in update mode, then fetch prescription, load drug frequency choice items and set the screen to progress state`() {
    val prescribedDrugUuid = UUID.fromString("e046a649-dfc0-45b5-89d4-7a4b0af1c282")
    val model = CustomDrugEntryModel.default(openAs = OpenAs.Update(prescribedDrugUuid = prescribedDrugUuid), dosagePlaceholder)

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model.customDrugEntryProgressStateLoading()),
                hasEffects(FetchPrescription(prescribedDrugUuid), LoadDrugFrequencyChoiceItems)
            )
        )
  }
}
