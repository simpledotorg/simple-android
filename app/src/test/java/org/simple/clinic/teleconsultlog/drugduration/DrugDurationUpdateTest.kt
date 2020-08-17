package org.simple.clinic.teleconsultlog.drugduration

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class DrugDurationUpdateTest {

  @Test
  fun `hide drug duration error when drug duration changes`() {
    val updateSpec = UpdateSpec(DrugDurationUpdate())
    updateSpec
        .given(DrugDurationModel.create())
        .whenEvent(DurationChanged)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(HideDurationError as DrugDurationEffect)
        ))
  }
}
