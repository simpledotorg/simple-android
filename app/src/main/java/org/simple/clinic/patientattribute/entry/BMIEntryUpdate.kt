package org.simple.clinic.patientattribute.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.patientattribute.BMIReading

class BMIEntryUpdate : Update<BMIEntryModel, BMIEntryEvent, BMIEntryEffect> {
  override fun update(
      model: BMIEntryModel,
      event: BMIEntryEvent
  ): Next<BMIEntryModel, BMIEntryEffect> {
    return when (event) {
      is SaveClicked -> dispatch(CreateNewBMIEntry(model.patientUUID, BMIReading(height = model.height, weight = model.weight)))
    }
  }
}
