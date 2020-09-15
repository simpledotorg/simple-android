package org.simple.clinic.teleconsultlog.prescription

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class TeleconsultPrescriptionUpdateTest {

  private val patientUuid = UUID.fromString("11c91ee8-2165-4429-962b-70c4951eddd0")
  private val model = TeleconsultPrescriptionModel.create(patientUuid)

  private val updateSpec = UpdateSpec(TeleconsultPrescriptionUpdate())

  @Test
  fun `when patient details are loaded, then update the model`() {
    val patient = TestData.patient(uuid = patientUuid)

    updateSpec
        .given(model)
        .whenEvent(PatientDetailsLoaded(patient))
        .then(assertThatNext(
            hasModel(model.patientLoaded(patient)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when back is clicked, then go back to previous screen`() {
    updateSpec
        .given(model)
        .whenEvent(BackClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBack)
        ))
  }
}
