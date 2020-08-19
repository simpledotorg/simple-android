package org.simple.clinic.teleconsultlog.success

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.LoadPatientDetails
import java.util.UUID

class TeleConsultSuccessInitTest {
  private val initSpec = InitSpec(TeleConsultSuccessInit())
  private val patientUuid = UUID.fromString("9cb22d70-eac6-4cf9-abe5-a5cc8c7773f6")
  private val model = TeleConsultSuccessModel(patientUuid, null)

  @Test
  fun `when the screen is created, load the patient details`() {
    initSpec
        .whenInit(model)
        .then {
          assertThatFirst(
              hasModel(model),
              hasEffects(LoadPatientDetails(patientUuid = patientUuid))
          )
        }
  }
}
