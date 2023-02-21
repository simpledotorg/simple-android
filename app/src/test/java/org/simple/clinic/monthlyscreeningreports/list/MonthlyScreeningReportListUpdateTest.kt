package org.simple.clinic.monthlyscreeningreports.list

import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class MonthlyScreeningReportListUpdateTest {

  private val defaultModel = MonthlyScreeningReportListModel.default()

  private val update = MonthlyScreeningReportListUpdate()

  private val spec = UpdateSpec(update)

  @Test
  fun `when questionnaire response list is fetched, then update the model`() {
    val responseList = TestData.questionnaireResponse()

    spec
        .given(defaultModel)
        .whenEvent(MonthlyScreeningReportListFetched(listOf(responseList)))
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasModel(defaultModel.responseListLoaded(listOf(responseList))),
            NextMatchers.hasNoEffects()
        ))
  }

  @Test
  fun `when current facility is loaded, then update the model`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("7dc68c5a-952d-46e7-83b1-070ce3d32600")
    )

    spec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasModel(defaultModel.currentFacilityLoaded(facility)),
            NextMatchers.hasNoEffects()
        ))
  }
}
