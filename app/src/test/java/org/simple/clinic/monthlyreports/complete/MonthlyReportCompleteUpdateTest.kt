package org.simple.clinic.monthlyreports.complete

import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.junit.Test
import org.simple.sharedTestCode.TestData

class MonthlyReportCompleteUpdateTest {

  private val defaultModel = MonthlyReportCompleteModel.default()

  private val update = MonthlyReportCompleteUpdate()

  private val spec = UpdateSpec(update)

  @Test
  fun `when questionnaire response is fetched, then update the model`() {
    val questionnaireResponse = TestData.questionnaireResponse()

    spec
        .given(defaultModel)
        .whenEvent(QuestionnaireResponseFetched(questionnaireResponse))
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasModel(defaultModel.questionnaireResponseLoaded(questionnaireResponse)),
            NextMatchers.hasNoEffects()
        ))
  }

  @Test
  fun `when done btn is clicked, then go to monthly report list screen`() {
    spec
        .given(defaultModel)
        .whenEvent(DoneButtonClicked)
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasNoModel(),
            NextMatchers.hasEffects(GoToMonthlyReportListScreen)
        ))
  }
}
