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
    val duration = "10"

    updateSpec
        .given(DrugDurationModel.create(duration))
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
        .given(DrugDurationModel.create(duration))
        .whenEvent(DrugDurationSaveClicked(duration))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBlankDurationError as DrugDurationEffect)
        ))
  }

  @Test
  fun `save drug duration when drug duration is not empty`() {
    val duration = "20"

    updateSpec
        .given(DrugDurationModel.create(duration))
        .whenEvent(DrugDurationSaveClicked(duration))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveDrugDuration(duration.toInt()) as DrugDurationEffect)
        ))
  }
}
