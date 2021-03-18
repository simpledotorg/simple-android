package org.simple.clinic.home.report

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class ReportsUpdateTest {

  @Test
  fun `when back button in reports web view is clicked, then load reports`() {
    val model = ReportsModel.create()

    UpdateSpec(ReportsUpdate())
        .given(model)
        .whenEvent(WebBackClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadReports)
        ))
  }
}
