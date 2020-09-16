package org.simple.clinic.teleconsultlog.prescription.medicines

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class TeleconsultMedicinesUpdateTest {

  @Test
  fun `when patient medicines are loaded, then update the model`() {
    val patientUuid = UUID.fromString("134a6669-7b4c-42dd-a763-e3015b7933ff")
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

    val model = TeleconsultMedicinesModel.create(patientUuid = patientUuid)

    UpdateSpec(TeleconsultMedicinesUpdate())
        .given(model)
        .whenEvent(PatientMedicinesLoaded(medicines))
        .then(assertThatNext(
            hasModel(model.medicinesLoaded(medicines)),
            hasNoEffects()
        ))
  }
}
