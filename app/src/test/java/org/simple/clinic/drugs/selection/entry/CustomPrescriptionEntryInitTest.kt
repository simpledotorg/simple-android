package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.drugs.selection.entry.OpenAs.New
import org.simple.clinic.drugs.selection.entry.OpenAs.Update
import java.util.UUID

class CustomPrescriptionEntryInitTest {

  private val initSpec = InitSpec(CustomPrescriptionEntryInit())

  @Test
  fun `when the sheet is opened for recording a new prescription, update the UI`() {
    val defaultModel = CustomPrescriptionEntryModel.create(New(UUID.fromString("0a0ff46c-cfeb-4f7c-9e8d-daeac27c452d")))

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
    val patientUuid = UUID.fromString("f1ba8c5d-cef8-4f0c-bef1-68252eafd76f")
    val prescribedDrugUuid = UUID.fromString("f9e77712-87f1-4adc-95c9-ebf59b24d816")
    val defaultModel = CustomPrescriptionEntryModel.create(Update(patientUuid, prescribedDrugUuid))

    initSpec
        .whenInit(defaultModel)
        .then(
            assertThatFirst(
                hasModel(defaultModel),
                hasEffects(FetchPrescription(prescribedDrugUuid) as CustomPrescriptionEntryEffect)
            )
        )
  }
}
