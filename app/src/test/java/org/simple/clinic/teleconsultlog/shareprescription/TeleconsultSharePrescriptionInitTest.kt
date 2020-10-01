package org.simple.clinic.teleconsultlog.shareprescription

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class TeleconsultSharePrescriptionInitTest {

  val patientUuid: UUID = UUID.fromString("d80927dc-e4f2-4224-a897-9352042115a9")
  val model = TeleconsultSharePrescriptionModel.create(patientUuid = patientUuid)

  @Test
  fun `when screen is created, load the patient details`() {
    val initSpec = InitSpec(TeleconsultSharePrescriptionInit())
    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(LoadPatientDetails(patientUuid))
            )
        )
  }
}
