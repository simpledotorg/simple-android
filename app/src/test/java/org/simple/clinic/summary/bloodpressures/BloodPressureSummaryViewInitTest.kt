package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class BloodPressureSummaryViewInitTest {
  @Test
  fun `when widget is created, then load the current facility and blood pressures count`() {
    val patientUuid = UUID.fromString("33d7f6d7-39f9-46a2-a104-457e5f77dc20")
    val defaultModel = BloodPressureSummaryViewModel.create(patientUuid)
    val initSpec = InitSpec<BloodPressureSummaryViewModel, BloodPressureSummaryViewEffect>(BloodPressureSummaryViewInit())

    initSpec
        .whenInit(defaultModel)
        .then(
            assertThatFirst(
                hasModel(defaultModel),
                hasEffects(
                    LoadBloodPressuresCount(patientUuid),
                    LoadCurrentFacility
                )
            )
        )
  }
}
