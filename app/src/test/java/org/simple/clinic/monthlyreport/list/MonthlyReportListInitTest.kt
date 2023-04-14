package org.simple.clinic.monthlyreport.list

import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import org.junit.Test
import org.simple.clinic.questionnaire.MonthlyScreeningReports

class MonthlyReportListInitTest {
  private val questionnaireType = MonthlyScreeningReports
  private val spec = InitSpec(MonthlyReportListInit(questionnaireType))

  @Test
  fun `when screen is created, then load initial data`() {
    val defaultModel = MonthlyReportListModel.default()

    spec
        .whenInit(defaultModel)
        .then(InitSpec.assertThatFirst(
            FirstMatchers.hasModel(defaultModel),
            FirstMatchers.hasEffects(
                LoadCurrentFacility,
                LoadMonthlyReportListEffect(questionnaireType),
            )
        ))
  }
}
