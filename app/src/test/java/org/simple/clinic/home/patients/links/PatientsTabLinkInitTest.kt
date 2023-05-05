package org.simple.clinic.home.patients.links

import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import org.junit.Test

class PatientsTabLinkInitTest {

  private val defaultModel = PatientsTabLinkModel.default()


  @Test
  fun `when screen is created, then load initial data`() {
    val initSpec = InitSpec(PatientsTabLinkInit())

    initSpec
        .whenInit(defaultModel)
        .then(InitSpec.assertThatFirst(
            FirstMatchers.hasModel(defaultModel),
            FirstMatchers.hasEffects(
                LoadCurrentFacility,
                LoadQuestionnaires,
                LoadQuestionnairesResponses
            )
        ))
  }
}
