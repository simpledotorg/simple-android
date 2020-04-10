package org.simple.clinic.bp.history

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class BloodPressureHistoryScreenInitTest {
  @Test
  fun `when screen is created, then load patient and all blood pressures`() {
    val patientUuid = UUID.fromString("90357d41-34dc-415a-884e-a1a58a199c64")
    val model = BloodPressureHistoryScreenModel.create(patientUuid)
    val initSpec = InitSpec(BloodPressureHistoryScreenInit())
    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(LoadPatient(model.patientUuid), ShowBloodPressures(model.patientUuid))
            )
        )
  }
}
