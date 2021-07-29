package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class CustomDrugEntryInitTest {
  private val initSpec = InitSpec(CustomDrugEntryInit())

  @Test
  fun `when the sheet is opened for recording a new prescription, update the UI`() {
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.New(patientUuid = UUID.fromString("8e3fc4b5-7da3-402c-bcdf-5edaa5f1ee2c")),
        drug = null,
        drugName = "Amlodipine")

    initSpec
        .whenInit(defaultModel)
        .then(
            assertThatFirst(
                hasModel(defaultModel),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when the sheet is opened for changing an existing prescription, fetch the existing prescription`() {
    val prescribedDrugUuid = UUID.fromString("3e527b7d-59b8-4d19-96f6-3654e3b32713")
    val defaultModel = CustomDrugEntryModel.default(openAs = OpenAs.Update(patientUuid = UUID.fromString("8e3fc4b5-7da3-402c-bcdf-5edaa5f1ee2c"), prescribedDrugUuid = prescribedDrugUuid),
        drug = null,
        drugName = "Amlodipine")

    initSpec
        .whenInit(defaultModel)
        .then(
            assertThatFirst(
                hasModel(defaultModel),
                hasEffects(FetchPrescription(prescribedDrugUuid))
            )
        )
  }
}
