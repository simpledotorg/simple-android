package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import java.util.UUID

class BloodPressureSummaryViewUpdateTest {
  private val patientUuid = UUID.fromString("8f1befda-f99e-4d26-aff3-cecb90925df1")
  private val defaultModel = BloodPressureSummaryViewModel.create(patientUuid)
  private val updateSpec = UpdateSpec<BloodPressureSummaryViewModel, BloodPressureSummaryViewEvent, BloodPressureSummaryViewEffect>(BloodPressureSummaryViewUpdate())

  @Test
  fun `when blood pressures are loaded, then show blood pressures`() {
    val bloodPressure1 = PatientMocker.bp(UUID.fromString("8815d0fc-73cc-44a2-a4b3-473c4c0989aa"))
    val bloodPressure2 = PatientMocker.bp(UUID.fromString("ddf87db7-1034-4618-bc0e-879d7d357adf"))
    val bloodPressures = listOf(bloodPressure1, bloodPressure2)

    updateSpec
        .given(defaultModel)
        .whenEvent(BloodPressuresLoaded(bloodPressures))
        .then(
            assertThatNext(
                hasModel(defaultModel.bloodPressuresLoaded(bloodPressures)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when blood pressures count is loaded, then change the blood pressures count in model`() {
    val bloodPressuresCount = 4

    updateSpec
        .given(defaultModel)
        .whenEvent(BloodPressuresCountLoaded(bloodPressuresCount))
        .then(
            assertThatNext(
                hasModel(defaultModel.bloodPressuresCountLoaded(bloodPressuresCount)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when current facility is loaded, then update the model`() {
    val facility = PatientMocker.facility(uuid = UUID.fromString("0729cf58-73b8-4be9-b9d7-87cbb6ee0f6b"))

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.currentFacilityLoaded(facility)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when add new blood pressure is clicked, then open blood pressure entry sheet`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(AddNewBloodPressureClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenBloodPressureEntrySheet(patientUuid) as BloodPressureSummaryViewEffect)
        ))
  }

  @Test
  fun `when blood pressure is clicked, then open blood pressure update sheet`() {
    val bloodPressureMeasurement = PatientMocker.bp(
        UUID.fromString("88ed645b-7b00-4a72-81bb-94fba4474523"),
        patientUuid
    )
    val bloodPressures = listOf(bloodPressureMeasurement)

    updateSpec
        .given(defaultModel.bloodPressuresLoaded(bloodPressures))
        .whenEvent(BloodPressureClicked(bloodPressureMeasurement))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenBloodPressureUpdateSheet(bloodPressureMeasurement) as BloodPressureSummaryViewEffect)
        ))
  }

  @Test
  fun `when see all is clicked, then show blood pressure history screen`() {
    val bloodPressure1 = PatientMocker.bp(
        UUID.fromString("509ae85b-f7d5-48a6-9dfc-a6e4bae00cce"),
        patientUuid
    )
    val bloodPressure2 = PatientMocker.bp(
        UUID.fromString("c1cba6aa-4cff-4809-9654-05c7c5b8fcd0"),
        patientUuid
    )
    val bloodPressure3 = PatientMocker.bp(
        UUID.fromString("4ca650eb-706c-4c8e-8e7d-f0a5f41e99e6"),
        patientUuid
    )
    val bloodPressure4 = PatientMocker.bp(
        UUID.fromString("1bd8492b-ca4c-4ae6-a4ce-0034818da775"),
        patientUuid
    )
    val bloodPressures = listOf(bloodPressure1, bloodPressure2, bloodPressure3, bloodPressure4)

    updateSpec
        .given(defaultModel.bloodPressuresLoaded(bloodPressures))
        .whenEvent(SeeAllClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBloodPressureHistoryScreen(patientUuid) as BloodPressureSummaryViewEffect)
        ))
  }
}
