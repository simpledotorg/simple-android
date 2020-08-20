package org.simple.clinic.teleconsultlog.medicinefrequency

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class MedicineFrequencyInitTest {

  private val medicineFrequency = MedicineFrequency.BD
  private val initSpec = InitSpec(MedicineFrequencyInit())
  private val model = MedicineFrequencyModel.create(medicineFrequency)

  @Test
  fun `when the sheet is created, load the default medicine frequency`() {

    initSpec
        .whenInit(model)
        .then {
          assertThatFirst(
              hasModel(model),
              hasEffects(LoadDefaultMedicineFrequency(medicineFrequency))
          )
        }
  }
}
