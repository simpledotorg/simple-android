package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class NewMedicalHistoryInitTest {

  @Test
  fun `when the screen is created, the ongoing patient entry and the current facility must be loaded`() {
    val initSpec = InitSpec(NewMedicalHistoryInit())

    val model = NewMedicalHistoryModel.default()

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(LoadOngoingPatientEntry, LoadCurrentFacility)
            )
        )
  }
}
