package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class BloodSugarSummaryViewInitTest {

  @Test
  fun `when the widget is created then blood sugars for patient should be fetched`() {
    val initSpec = InitSpec<BloodSugarSummaryViewModel, BloodSugarSummaryViewEffect>(BloodSugarSummaryViewInit())
    val defaultModel = BloodSugarSummaryViewModel.create(patientUuid = UUID.fromString("f9694e07-a780-4e9a-a0d5-7f278b68a46e"))

    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(
                FetchBloodSugarSummary(defaultModel.patientUuid),
                FetchBloodSugarCount(defaultModel.patientUuid)
            )
        ))
  }
}
