package org.simple.clinic.home.patients.links

import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class PatientsTabLinkUpdateTest {
  private val defaultModel = PatientsTabLinkModel.default()
  private val updateSpec = UpdateSpec(PatientsTabLinkUpdate())

  @Test
  fun `when current facility is loaded, then update the model`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("7dc68c5a-952d-46e7-83b1-070ce3d32600")
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasModel(defaultModel.currentFacilityLoaded(facility)),
            NextMatchers.hasNoEffects()
        ))
  }

  @Test
  fun `when monthly screening report list button is clicked, then open monthly screening report list screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(MonthlyScreeningReportsClicked)
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasNoModel(),
            NextMatchers.hasEffects(OpenMonthlyScreeningReportsListScreen)
        ))
  }

  @Test
  fun `when patient line list download button is clicked, then open patient line list download dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DownloadPatientLineListClicked())
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasNoModel(),
            NextMatchers.hasEffects(OpenPatientLineListDownloadDialog)
        ))
  }
}
