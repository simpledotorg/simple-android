package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import java.util.UUID

class BloodSugarSummaryViewUpdateTest {

  private val spec = UpdateSpec<BloodSugarSummaryViewModel, BloodSugarSummaryViewEvent, BloodSugarSummaryViewEffect>(BloodSugarSummaryViewUpdate())
  private val patientUuid = UUID.fromString("6a955cca-929f-4466-80cf-f2190dd57ce7")
  private val defaultModel = BloodSugarSummaryViewModel.create(patientUuid = patientUuid)

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

  @Test
  fun `when see all is clicked, then show blood sugar history screen`() {
    val bloodSugar1 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("509ae85b-f7d5-48a6-9dfc-a6e4bae00cce"),
        patientUuid = patientUuid
    )
    val bloodSugar2 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("509ae85b-f7d5-48a6-9dfc-a6e4bae00cce"),
        patientUuid = patientUuid
    )
    val bloodSugar3 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("509ae85b-f7d5-48a6-9dfc-a6e4bae00cce"),
        patientUuid = patientUuid
    )
    val bloodSugar4 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("509ae85b-f7d5-48a6-9dfc-a6e4bae00cce"),
        patientUuid = patientUuid
    )
    val bloodSugars = listOf(bloodSugar1, bloodSugar2, bloodSugar3, bloodSugar4)

    spec
        .given(defaultModel.summaryFetched(bloodSugars))
        .whenEvent(SeeAllClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBloodSugarHistoryScreen(patientUuid) as BloodSugarSummaryViewEffect)
        ))
  }

  @Test
  fun `when blood sugar is clicked, then open blood sugar update sheet`() {
    val bloodSugarMeasurement = TestData.bloodSugarMeasurement(
        UUID.fromString("9a82720a-0445-43dd-b557-3d4b079b66ef"),
        patientUuid = patientUuid
    )
    val bloodSugars = listOf(bloodSugarMeasurement)

    spec
        .given(defaultModel.summaryFetched(bloodSugars))
        .whenEvent(BloodSugarClicked(bloodSugarMeasurement))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenBloodSugarUpdateSheet(bloodSugarMeasurement) as BloodSugarSummaryViewEffect)
        ))
  }

}
