package org.simple.clinic.teleconsultlog.prescription

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class TeleconsultPrescriptionUpdateTest {

  @Test
  fun `when patient details are loaded, then update the model`() {
    val patientUuid = UUID.fromString("11c91ee8-2165-4429-962b-70c4951eddd0")
    val patient = TestData.patient(uuid = patientUuid)
    val model = TeleconsultPrescriptionModel.create(patientUuid)

    UpdateSpec(TeleconsultPrescriptionUpdate())
        .given(model)
        .whenEvent(PatientDetailsLoaded(patient))
        .then(assertThatNext(
            hasModel(model.patientLoaded(patient)),
            hasNoEffects()
        ))
  }
}
