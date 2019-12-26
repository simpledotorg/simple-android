package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class BloodSugarSummaryViewUpdateTest {

  @Test
  fun `when blood sugar is loaded then ui must be updated`() {
    val spec = UpdateSpec<BloodSugarSummaryViewModel, BloodSugarSummaryViewEvent, BloodSugarSummaryViewEffect>(BloodSugarSummaryViewUpdate())
    val defaultModel = BloodSugarSummaryViewModel.EMPTY

    spec
        .given(defaultModel)
        .whenEvent(BloodSugarSummaryFetched)
        .then(assertThatNext(
            hasModel(defaultModel.summaryFetched()),
            hasNoEffects()
        ))
  }
}
