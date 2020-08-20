package org.simple.clinic.teleconsultlog.medicinefrequency

import com.spotify.mobius.First
import com.spotify.mobius.Init

class MedicineFrequencyInit : Init<MedicineFrequencyModel, MedicineFrequencyEffect> {
  override fun init(model: MedicineFrequencyModel): First<MedicineFrequencyModel, MedicineFrequencyEffect> {
    val effects = mutableSetOf<MedicineFrequencyEffect>()
    effects.add(LoadDefaultMedicineFrequency(model.medicineFrequency))

    return First.first(model, effects)
  }
}
