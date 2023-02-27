package org.simple.clinic.home.patients.links

import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.junit.Test

class PatientsTabLinkUpdateTest {
  private val defaultModel = PatientsTabLinkModel.default()
  private val updateSpec = UpdateSpec(PatientsTabLinkUpdate())

  @Test
  fun `when patient line list download button is clicked, then open patient line list download dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DownloadPatientLineListClicked())
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasNoModel(),
            NextMatchers.hasEffects(OpenPatientLineListDownloadDialog)
        ))
  }
}
