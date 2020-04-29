package org.simple.clinic.bloodsugar.history

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class BloodSugarHistoryScreenInitTest {
  @Test
  fun `when screen is created, then load patient and all blood sugars`() {
    val patientUuid = UUID.fromString("4bc4432f-f01e-4d0b-80bf-bc8a48ece8fe")
    val model = BloodSugarHistoryScreenModel.create(patientUuid)
    val initSpec = InitSpec(BloodSugarHistoryScreenInit())

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadPatient(patientUuid), ShowBloodSugars(model.patientUuid))
        ))
  }
}
