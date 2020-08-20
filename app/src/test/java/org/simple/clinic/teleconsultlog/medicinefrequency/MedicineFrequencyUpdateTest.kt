package org.simple.clinic.teleconsultlog.medicinefrequency

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class MedicineFrequencyUpdateTest {

  private val updateSpec = UpdateSpec(MedicineFrequencyUpdate())
  private val model = MedicineFrequencyModel.create(MedicineFrequency.BD)

  @Test
  fun `when save button is clicked, then save the medicine frequency`() {
    updateSpec
        .given(model)
        .whenEvents(SaveMedicineFrequencyClicked(MedicineFrequency.BD))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(SaveMedicineFrequency(MedicineFrequency.BD) as MedicineFrequencyEffect)
            )
        )
  }
}
