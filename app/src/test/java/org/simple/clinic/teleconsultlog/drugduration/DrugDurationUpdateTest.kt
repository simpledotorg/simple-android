package org.simple.clinic.teleconsultlog.drugduration

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class DrugDurationUpdateTest {

  private val updateSpec = UpdateSpec(DrugDurationUpdate())

  @Test
  fun `hide drug duration error when drug duration changes`() {
    updateSpec
        .given(DrugDurationModel.create())
        .whenEvent(DurationChanged)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(HideDurationError as DrugDurationEffect)
        ))
  }

  @Test
  fun `show drug duration error when drug duration is empty`() {
    val duration = ""

    updateSpec
        .given(DrugDurationModel.create())
        .whenEvent(DrugDurationSaveClicked(duration))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBlankDurationError as DrugDurationEffect)
        ))
  }
}
