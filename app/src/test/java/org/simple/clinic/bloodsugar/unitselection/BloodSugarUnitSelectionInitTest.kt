package org.simple.clinic.bloodsugar.unitselection

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference

class BloodSugarUnitSelectionInitTest {

  @Test
  fun `when the dialog is opened, load the blood sugar unit preference`() {
    val bloodSugarUnitPreferenceSelected = BloodSugarUnitPreference.Mmol
    val model = BloodSugarUnitSelectionModel.create(bloodSugarUnitPreferenceSelected)
    InitSpec(BloodSugarUnitSelectionInit())
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(PreFillBloodSugarUnitSelected(bloodSugarUnitPreferenceSelected))
            )
        )
  }
}
