package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class CustomPrescriptionEntryUpdate : Update<CustomPrescriptionEntryModel, CustomPrescriptionEntryEvent, CustomPrescriptionEntryEffect> {

  override fun update(model: CustomPrescriptionEntryModel, event: CustomPrescriptionEntryEvent):
      Next<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {
    return when (event) {
      is CustomPrescriptionDrugNameTextChanged -> next(model.drugNameChanged(event.name))
    }
  }
}
