package org.simple.clinic.teleconsultlog.drugduration

import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class DrugDurationInitTest {

  @Test
  fun `when screen is created and drug duration is available then set drug duration`() {
    val initSpec = InitSpec(DrugDurationInit())
    val duration = "10"
    val model = DrugDurationModel.create(duration)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasNoEffects()
        ))
  }
}
