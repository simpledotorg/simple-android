package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.summary.bloodpressures.LoadBloodPressures
import org.simple.clinic.summary.bloodpressures.LoadBloodPressuresCount
import org.simple.clinic.summary.bloodpressures.NewBloodPressureSummaryViewConfig
import org.simple.clinic.summary.bloodpressures.NewBloodPressureSummaryViewEffect
import org.simple.clinic.summary.bloodpressures.NewBloodPressureSummaryViewInit
import org.simple.clinic.summary.bloodpressures.NewBloodPressureSummaryViewModel
import java.util.UUID

class NewBloodPressureSummaryViewInitTest {
  @Test
  fun `when widget is created, then load blood pressures for patient`() {
    val patientUuid = UUID.fromString("33d7f6d7-39f9-46a2-a104-457e5f77dc20")
    val defaultModel = NewBloodPressureSummaryViewModel.create(patientUuid)
    val config = NewBloodPressureSummaryViewConfig(numberOfBpsToDisplay = 3)
    val initSpec = InitSpec<NewBloodPressureSummaryViewModel, NewBloodPressureSummaryViewEffect>(NewBloodPressureSummaryViewInit(config))

    initSpec
        .whenInit(defaultModel)
        .then(
            assertThatFirst(
                hasModel(defaultModel),
                hasEffects(LoadBloodPressures(patientUuid, config.numberOfBpsToDisplay), LoadBloodPressuresCount(patientUuid))
            )
        )
  }
}
