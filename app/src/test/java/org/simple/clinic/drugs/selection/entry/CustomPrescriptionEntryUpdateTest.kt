package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import java.util.UUID

class CustomPrescriptionEntryUpdateTest {

  @Test
  fun `when custom prescription is saved, then close the sheet`() {
    val updateSpec = UpdateSpec(
        CustomPrescriptionEntryUpdate()
    )
    val patientUuid = UUID.fromString("c8e56cb8-e09f-41ae-ab87-50d6f533574a")
    val prescriptionUuid = UUID.fromString("324bbd5f-3f05-407f-b020-de2b8521d8ef")
    val model = CustomPrescriptionEntryModel
        .create(OpenAs.Update(patientUuid, prescriptionUuid))
        .drugNameChanged("Atenolol")
        .dosageChanged("10 mg")

    updateSpec
        .given(model)
        .whenEvent(CustomPrescriptionSaved)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseSheet as CustomPrescriptionEntryEffect)
        ))
  }
}
