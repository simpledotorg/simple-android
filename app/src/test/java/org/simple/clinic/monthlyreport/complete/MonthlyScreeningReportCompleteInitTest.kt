package org.simple.clinic.monthlyreport.complete

import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import org.junit.Test
import org.simple.sharedTestCode.TestData

class MonthlyScreeningReportCompleteInitTest {
  private val questionnaireResponse = TestData.questionnaireResponse()

  private val spec = InitSpec(MonthlyScreeningReportCompleteInit(questionnaireResponse.uuid))

  @Test
  fun `when screen is created, then load initial data`() {
    val defaultModel = MonthlyScreeningReportCompleteModel.default()

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
