package org.simple.clinic.monthlyreports.list

import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import org.junit.Test
import org.simple.clinic.questionnaire.MonthlyScreeningReports

class MonthlyReportsInitTest {
  private val questionnaireType = MonthlyScreeningReports
  private val spec = InitSpec(MonthlyReportsInit(questionnaireType))

  @Test
  fun `when screen is created, then load initial data`() {
    val defaultModel = MonthlyReportsModel.default()

    spec
        .whenInit(defaultModel)
        .then(InitSpec.assertThatFirst(
            FirstMatchers.hasModel(defaultModel),
            FirstMatchers.hasEffects(
                LoadCurrentFacility,
                LoadMonthlyReportsEffect(questionnaireType),
            )
        ))
  }
}
