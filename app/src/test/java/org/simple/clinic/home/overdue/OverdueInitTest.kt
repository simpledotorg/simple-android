package org.simple.clinic.home.overdue

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class OverdueInitTest {

  @Test
  fun `when overdue screen is created, then load current facility and selected appointment ids`() {
    val defaultModel = OverdueModel.create()

    InitSpec(OverdueInit())
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadCurrentFacility, LoadSelectedOverdueAppointmentIds)
        ))
  }
}
