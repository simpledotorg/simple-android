package org.simple.clinic.home.patients.links

import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import org.junit.Test

class PatientsTabLinkInitTest {

  private val defaultModel = PatientsTabLinkModel.default()


  @Test
  fun `when screen is created, and monthly screening report feature flag is enabled, then load questionnaire data`() {
    val initSpec = InitSpec(PatientsTabLinkInit(
        isMonthlyScreeningReportsEnabled = true
    ))

    initSpec
        .whenInit(defaultModel)
        .then(InitSpec.assertThatFirst(
            FirstMatchers.hasModel(defaultModel),
            FirstMatchers.hasEffects(
                LoadCurrentFacility,
                LoadMonthlyScreeningReportResponseList,
                LoadMonthlyScreeningReportForm
            )
        ))
  }

  @Test
  fun `when screen is created, and monthly screening report feature flag is disabled, then don't load questionnaire data`() {
    val initSpec = InitSpec(PatientsTabLinkInit(
        isMonthlyScreeningReportsEnabled = false
    ))

    initSpec
        .whenInit(defaultModel)
        .then(InitSpec.assertThatFirst(
            FirstMatchers.hasModel(defaultModel),
            FirstMatchers.hasNoEffects()
        ))
  }
}
