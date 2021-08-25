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
  fun `when sheet is created in create mode from the drug list, fetch drug from the drugUuid`() {
    val drugUuid = UUID.fromString("6106544f-2b18-410d-992b-81860a08f02a")
    val model = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugList(drugUuid), dosagePlaceholder)

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(FetchDrug(drugUuid))
            )
        )
  }

  @Test
  fun `when sheet is created in create mode from a drug name, then update the drug name in the model`() {
    val model = CustomDrugEntryModel.default(openAs = OpenAs.New.FromDrugName(drugName), dosagePlaceholder)

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model.drugNameLoaded(drugName))
            )
        )
  }

  @Test
  fun `when sheet is created in update mode, then fetch prescription`() {
    val prescribedDrugUuid = UUID.fromString("e046a649-dfc0-45b5-89d4-7a4b0af1c282")
    val model = CustomDrugEntryModel.default(openAs = OpenAs.Update(prescribedDrugUuid = prescribedDrugUuid), dosagePlaceholder)

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasEffects(FetchPrescription(prescribedDrugUuid))
            )
        )
  }
}
