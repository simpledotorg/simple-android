package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import java.util.UUID

class BloodSugarSummaryViewUpdateTest {

  private val spec = UpdateSpec<BloodSugarSummaryViewModel, BloodSugarSummaryViewEvent, BloodSugarSummaryViewEffect>(BloodSugarSummaryViewUpdate())
  private val defaultModel = BloodSugarSummaryViewModel.create(patientUuid = UUID.fromString("6a955cca-929f-4466-80cf-f2190dd57ce7"))

  @Test
  fun `when blood sugar is loaded then ui must be updated`() {
    val measurements = listOf<BloodSugarMeasurement>()
    spec
        .given(defaultModel)
        .whenEvent(BloodSugarSummaryFetched(measurements))
        .then(assertThatNext(
            hasModel(defaultModel.summaryFetched(measurements)),
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

  @Test
  fun `when blood sugars count is loaded, then change the blood sugars count in model`() {
    val bloodSugarsCount = 4

    spec
        .given(defaultModel)
        .whenEvent(BloodSugarCountFetched(bloodSugarsCount))
        .then(assertThatNext(
            hasModel(defaultModel.countFetched(bloodSugarsCount)),
            hasNoEffects()
        ))
  }
}
