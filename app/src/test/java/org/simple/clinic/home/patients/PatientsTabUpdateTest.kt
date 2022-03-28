package org.simple.clinic.home.patients

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class PatientsTabUpdateTest {
  private val defaultModel = PatientsTabModel.create()
  private val updateSpec = UpdateSpec(PatientsTabUpdate())

  @Test
  fun `when update now button is clicked, then open Simple on play store`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(UpdateNowButtonClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenSimpleOnPlayStore)
            )
        )
  }
}
