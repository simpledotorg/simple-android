package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityConfig
import java.util.UUID

class BloodPressureSummaryViewInitTest {
  private val patientUuid = UUID.fromString("33d7f6d7-39f9-46a2-a104-457e5f77dc20")
  private val defaultModel = BloodPressureSummaryViewModel.create(patientUuid)
  private val config = BloodPressureSummaryViewConfig(numberOfBpsToDisplay = 3, numberOfBpsToDisplayWithoutDiabetesManagement = 8)

  private val initSpec = InitSpec<BloodPressureSummaryViewModel, BloodPressureSummaryViewEffect>(BloodPressureSummaryViewInit(config))

  @Test
  fun `when widget is created, then load the current facility and blood pressures count`() {
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

  @Test
  fun `when the screen is restored and diabetes management is enabled, do not load the current facility and load fewer blood pressures`() {
    val bloodSugarsCount = 10
    val facility = TestData.facility(
        uuid = UUID.fromString("2b4d19e0-5291-4dc3-b7c9-704ebc1cfcd7"),
        facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
    )

    val model = defaultModel
        .bloodPressuresCountLoaded(bloodSugarsCount)
        .currentFacilityLoaded(facility)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(
                LoadBloodPressuresCount(patientUuid),
                LoadBloodPressures(patientUuid, config.numberOfBpsToDisplay)
            )
        ))
  }

  @Test
  fun `when the screen is restored and diabetes management is disabled, do not load the current facility and load fewer blood pressures`() {
    val bloodSugarsCount = 10
    val facility = TestData.facility(
        uuid = UUID.fromString("e67dd86c-5311-437d-b86d-ad6d2dd75c7e"),
        facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
    )

    val model = defaultModel
        .bloodPressuresCountLoaded(bloodSugarsCount)
        .currentFacilityLoaded(facility)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(
                LoadBloodPressuresCount(patientUuid),
                LoadBloodPressures(patientUuid, config.numberOfBpsToDisplayWithoutDiabetesManagement)
            )
        ))
  }
}
