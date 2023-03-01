package org.simple.clinic.monthlyscreeningreports.complete

import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.junit.Test

class MonthlyScreeningReportCompleteUpdateTest {

  private val defaultModel = MonthlyScreeningReportCompleteModel.default()

  private val update = MonthlyScreeningReportCompleteUpdate()

  private val spec = UpdateSpec(update)

  @Test
  fun `when done btn is clicked, then go to monthly screening report list screen`() {
    spec
        .given(defaultModel)
        .whenEvent(DoneButtonClicked)
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasNoModel(),
            NextMatchers.hasEffects(GoToMonthlyReportListScreen)
        ))
  }
}
