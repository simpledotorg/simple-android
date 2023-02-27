package org.simple.clinic.home.patients.links

import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import org.junit.Test

class PatientsTabLinkInitTest {

  private val defaultModel = PatientsTabLinkModel.default()


  @Test
  fun `when screen is created and patient line list feature flag is enabled, then load current facility`() {
    val initSpec = InitSpec(PatientsTabLinkInit())

    initSpec
        .whenInit(defaultModel)
        .then(InitSpec.assertThatFirst(
            FirstMatchers.hasModel(defaultModel),
            FirstMatchers.hasEffects(
                LoadMonthlyScreeningReportsFormEffect,
                LoadMonthlyScreeningReportsListEffect
            )
        ))
  }
}
