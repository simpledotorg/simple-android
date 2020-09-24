package org.simple.clinic.teleconsultlog.drugduration

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class DrugDurationInitTest {

  @Test
  fun `when screen is created, then prefill drug duration`() {
    val model = DrugDurationModel.create("35")

    InitSpec(DrugDurationInit())
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(PrefillDrugDuration("35"))
        ))
  }
}
