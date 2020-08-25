package org.simple.clinic.teleconsultlog.medicinefrequency

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class MedicineFrequencyUpdate : Update<MedicineFrequencyModel, MedicineFrequencyEvent, MedicineFrequencyEffect> {
  override fun update(model: MedicineFrequencyModel, event: MedicineFrequencyEvent): Next<MedicineFrequencyModel, MedicineFrequencyEffect> {
    return when (event) {
      is SaveMedicineFrequencyClicked -> dispatch(SaveMedicineFrequency(event.medicineFrequency))
      is MedicineFrequencyChanged -> next(model.medicineFrequencyChanged(event.medicineFrequency))
    }
  }
}
