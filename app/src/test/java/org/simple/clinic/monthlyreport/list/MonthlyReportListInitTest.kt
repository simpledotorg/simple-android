package org.simple.clinic.monthlyreport.list

import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import org.junit.Test

class MonthlyReportListInitTest {
  private val spec = InitSpec(MonthlyReportListInit())

  @Test
  fun `when screen is created, then load initial data`() {
    val defaultModel = MonthlyReportListModel.default()

    spec
        .whenInit(defaultModel)
        .then(InitSpec.assertThatFirst(
            FirstMatchers.hasModel(defaultModel),
            FirstMatchers.hasEffects(
                LoadCurrentFacility,
                LoadMonthlyReportListEffect,
            )
        ))
  }
}
