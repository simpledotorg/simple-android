package org.simple.clinic.drugs

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.OpenIntention.RefillMedicine
import java.util.UUID

class EditMedicineUpdateTest {
  @Test
  fun `when prescribed drugs refill done is clicked, then refill medicines`() {
    val patientUuid = UUID.fromString("871a2f40-2bda-488c-9443-7dc708c3743a")
    val updateSpec = UpdateSpec(EditMedicinesUpdate())
    val model = EditMedicinesModel.create(patientUuid, RefillMedicine)
    val prescribedDrugRecords = listOf(
        TestData.prescription(uuid = UUID.fromString("4aec376e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine1"),
        TestData.prescription(uuid = UUID.fromString("537a119e-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine2"),
        TestData.prescription(uuid = UUID.fromString("5ac2a678-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine3"),
        TestData.prescription(uuid = UUID.fromString("5f9f0fe2-1a8f-11eb-adc1-0242ac120002"), name = "Amlodipine4"),
    )

    val prescribedDrugsFetchedModel = model.prescribedDrugsFetched(prescribedDrugRecords)
    updateSpec
        .given(prescribedDrugsFetchedModel)
        .whenEvent(PresribedDrugsRefillClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(RefillMedicines(patientUuid)),
        ))
  }
}
