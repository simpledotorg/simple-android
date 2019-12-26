package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class BloodSugarSummaryViewUpdateTest {

  private val spec = UpdateSpec<BloodSugarSummaryViewModel, BloodSugarSummaryViewEvent, BloodSugarSummaryViewEffect>(BloodSugarSummaryViewUpdate())
  private val defaultModel = BloodSugarSummaryViewModel.EMPTY

  @Test
  fun `when blood sugar is loaded then ui must be updated`() {
    spec
        .given(defaultModel)
        .whenEvent(BloodSugarSummaryFetched)
        .then(assertThatNext(
            hasModel(defaultModel.summaryFetched()),
            hasNoEffects()
        ))
  }

  @Test
  fun `when add new blood sugar is clicked then blood sugar type selector should open`() {
    spec
        .given(defaultModel)
        .whenEvent(NewBloodSugarClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenBloodSugarTypeSelector as BloodSugarSummaryViewEffect)
        ))
  }
}
