package org.simple.clinic.monthlyscreeningreports.list

import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import org.junit.Test

class MonthlyScreeningReportListInitTest {
  private val spec = InitSpec(MonthlyScreeningReportListInit())

  @Test
  fun `when screen is created, then load initial data`() {
    val defaultModel = MonthlyScreeningReportListModel.default()

    spec
        .whenInit(defaultModel)
        .then(InitSpec.assertThatFirst(
            FirstMatchers.hasModel(defaultModel),
            FirstMatchers.hasEffects(
                LoadCurrentFacility,
                LoadMonthlyScreeningReportListEffect,
            )
        ))
  }
}
