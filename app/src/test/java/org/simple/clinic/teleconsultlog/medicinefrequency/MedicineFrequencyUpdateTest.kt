package org.simple.clinic.teleconsultlog.medicinefrequency

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class MedicineFrequencyUpdateTest {

  private val updateSpec = UpdateSpec(MedicineFrequencyUpdate())
  private val currentMedicineFrequency = MedicineFrequency.BD
  private val model = MedicineFrequencyModel.create(currentMedicineFrequency)

  @Test
  fun `when save button is clicked, then save the medicine frequency`() {
    updateSpec
        .given(model)
        .whenEvents(SaveMedicineFrequencyClicked(currentMedicineFrequency))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(SaveMedicineFrequency(currentMedicineFrequency) as MedicineFrequencyEffect)
            )
        )
  }

  @Test
  fun `when medicine frequency is changed, update the medicine frequency`() {
    updateSpec
        .given(model)
        .whenEvents(MedicineFrequencyChanged(MedicineFrequency.QDS))
        .then(
            assertThatNext(
                hasModel(model.medicineFrequencyChanged(MedicineFrequency.QDS)),
                hasNoEffects()
            )
        )
  }
}
