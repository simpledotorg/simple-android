package org.simple.clinic.bloodsugar.unitselection

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference

class BloodSugarUnitSelectionUpdateTest {

  private val unitSelectionValue = BloodSugarUnitPreference.Mg
  private val model = BloodSugarUnitSelectionModel()
  private val updateSpec = UpdateSpec(BloodSugarUnitSelectionUpdate())

  @Test
  fun `when done button is clicked, update the preference and close the sheet`() {
    updateSpec
        .given(model)
        .whenEvent(DoneClicked(unitSelectionValue))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(SaveBloodSugarUnitSelection(bloodSugarUnitSelection = unitSelectionValue))
            )
        )
  }
}
