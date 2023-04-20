package org.simple.clinic.monthlyreports.complete

import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import org.junit.Test
import org.simple.sharedTestCode.TestData

class MonthlyReportCompleteInitTest {
  private val questionnaireResponse = TestData.questionnaireResponse()

  private val spec = InitSpec(MonthlyReportCompleteInit(questionnaireResponse.uuid))

  @Test
  fun `when screen is created, then load initial data`() {
    val defaultModel = MonthlyReportCompleteModel.default()

    spec
        .whenInit(defaultModel)
        .then(InitSpec.assertThatFirst(
            FirstMatchers.hasModel(defaultModel),
            FirstMatchers.hasEffects(
                LoadQuestionnaireResponseEffect(questionnaireResponse.uuid)
            )
        ))
  }
}
