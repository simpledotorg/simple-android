package org.simple.clinic.teleconsultlog.prescription.medicines

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class TeleconsultMedicinesUpdateTest {
  private val patientUuid = UUID.fromString("134a6669-7b4c-42dd-a763-e3015b7933ff")
  private val model = TeleconsultMedicinesModel.create(patientUuid = patientUuid)

  private val updateSpec = UpdateSpec(TeleconsultMedicinesUpdate())

  @Test
  fun `when patient medicines are loaded, then update the model`() {
    val medicines = listOf(
        TestData.prescription(
            uuid = UUID.fromString("9fde23c1-d932-4bef-877c-88ade55e359a"),
            patientUuid = patientUuid
        ),
        TestData.prescription(
            uuid = UUID.fromString("41753f87-3823-40b5-972f-9533539d8324"),
            patientUuid = patientUuid
        )
    )

    updateSpec
        .given(model)
        .whenEvent(PatientMedicinesLoaded(medicines))
        .then(assertThatNext(
            hasModel(model.medicinesLoaded(medicines)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when edit medicines is clicked, then open edit medicines`() {
    updateSpec
        .given(model)
        .whenEvent(EditMedicinesClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenEditMedicines(patientUuid))
        ))
  }

  @Test
  fun `when drug duration is clicked, then open drug duration sheet`() {
    val prescription = TestData.prescription(
        uuid = UUID.fromString("b9c52365-2782-4c03-95ac-1c508a11a510")
    )

    updateSpec
        .given(model)
        .whenEvent(DrugDurationClicked(prescription))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenDrugDurationSheet(prescription))
        ))
  }

  @Test
  fun `when drug frequency is clicked, then open drug frequency sheet`() {
    val prescription = TestData.prescription(
        uuid = UUID.fromString("b9c52365-2782-4c03-95ac-1c508a11a510")
    )

    updateSpec
        .given(model)
        .whenEvent(DrugFrequencyClicked(prescription))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenDrugFrequencySheet(prescription))
        ))
  }
}
