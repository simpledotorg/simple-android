package org.simple.clinic.monthlyreports.list

import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.junit.Test
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.sharedTestCode.TestData
import java.util.UUID

class MonthlyReportsUpdateTest {

  private val defaultModel = MonthlyReportsModel.default()

  private val update = MonthlyReportsUpdate()

  private val spec = UpdateSpec(update)

  @Test
  fun `when monthly reports is fetched, then update the model`() {
    val responseList = TestData.questionnaireResponse()

    spec
        .given(defaultModel)
        .whenEvent(MonthlyReportsFetched(listOf(responseList)))
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasModel(defaultModel.monthlyReportsLoaded(listOf(responseList))),
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

  @Test
  fun `when monthly screening report is clicked, then open monthly screening report form`() {
    val questionnaireResponse = TestData.questionnaireResponse()

    spec
        .given(defaultModel)
        .whenEvent(MonthlyReportItemClicked(MonthlyScreeningReports, questionnaireResponse))
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasNoModel(),
            NextMatchers.hasEffects(OpenMonthlyReportForm(MonthlyScreeningReports, questionnaireResponse))
        ))
  }
}
