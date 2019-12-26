package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class BloodSugarSummaryViewInitTest {

  @Test
  fun `when the widget is created then blood sugars for patient should be fetched`() {
    val initSpec = InitSpec<BloodSugarSummaryViewModel, BloodSugarSummaryViewEffect>(BloodSugarSummaryViewInit())
    val defaultModel = BloodSugarSummaryViewModel.EMPTY

    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(FetchBloodSugarSummary as BloodSugarSummaryViewEffect)
        ))
  }
}
