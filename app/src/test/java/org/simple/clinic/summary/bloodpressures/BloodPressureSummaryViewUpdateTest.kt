package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityConfig
import java.util.UUID

class BloodPressureSummaryViewUpdateTest {
  private val patientUuid = UUID.fromString("8f1befda-f99e-4d26-aff3-cecb90925df1")
  private val defaultModel = BloodPressureSummaryViewModel.create(patientUuid)
  private val config = BloodPressureSummaryViewConfig(numberOfBpsToDisplay = 3, numberOfBpsToDisplayWithoutDiabetesManagement = 8)
  private val updateSpec = UpdateSpec<BloodPressureSummaryViewModel, BloodPressureSummaryViewEvent, BloodPressureSummaryViewEffect>(BloodPressureSummaryViewUpdate(config))

  @Test
  fun `when blood pressures are loaded, then show blood pressures`() {
    val bloodPressure1 = TestData.bloodPressureMeasurement(UUID.fromString("8815d0fc-73cc-44a2-a4b3-473c4c0989aa"))
    val bloodPressure2 = TestData.bloodPressureMeasurement(UUID.fromString("ddf87db7-1034-4618-bc0e-879d7d357adf"))
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
  fun `when diabetes management is enabled, then load fewer number of blood pressures`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("0729cf58-73b8-4be9-b9d7-87cbb6ee0f6b"),
        facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.currentFacilityLoaded(facility)),
            hasEffects(LoadBloodPressures(patientUuid, config.numberOfBpsToDisplay) as BloodPressureSummaryViewEffect)
        ))
  }

  @Test
  fun `when diabetes management is disabled, then load larger number of blood pressures`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("924d5e18-039e-4e83-9f36-5a0974d8a299"),
        facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.currentFacilityLoaded(facility)),
            hasEffects(LoadBloodPressures(patientUuid, config.numberOfBpsToDisplayWithoutDiabetesManagement) as BloodPressureSummaryViewEffect)
        ))
  }

  @Test
  fun `when add new blood pressure is clicked, then open blood pressure entry sheet`() {
    val facility = TestData.facility(uuid = UUID.fromString("924d5e18-039e-4e83-9f36-5a0974d8a299"))

    updateSpec
        .given(defaultModel.currentFacilityLoaded(facility))
        .whenEvent(AddNewBloodPressureClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenBloodPressureEntrySheet(patientUuid, facility) as BloodPressureSummaryViewEffect)
        ))
  }

  @Test
  fun `when blood pressure is clicked, then open blood pressure update sheet`() {
    val bloodPressureMeasurement = TestData.bloodPressureMeasurement(
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
    val bloodPressure1 = TestData.bloodPressureMeasurement(
        UUID.fromString("509ae85b-f7d5-48a6-9dfc-a6e4bae00cce"),
        patientUuid
    )
    val bloodPressure2 = TestData.bloodPressureMeasurement(
        UUID.fromString("c1cba6aa-4cff-4809-9654-05c7c5b8fcd0"),
        patientUuid
    )
    val bloodPressure3 = TestData.bloodPressureMeasurement(
        UUID.fromString("4ca650eb-706c-4c8e-8e7d-f0a5f41e99e6"),
        patientUuid
    )
    val bloodPressure4 = TestData.bloodPressureMeasurement(
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
