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
      is HeightChanged -> onHeightChanged(model, event)
      is WeightChanged -> next(model.weightChanged(event.weight))
      is WeightBackspaceClicked -> onWeightBackSpaceClicked(model)
      is SaveClicked -> dispatch(CreateNewBMIEntry(model.patientUUID, BMIReading(height = model.height.toFloat(), weight = model.weight.toFloat())))
      is BackPressed -> dispatch(CloseSheet)
      is BMISaved -> dispatch(CloseSheet)
    }
  }

  private fun onHeightChanged(
      model: BMIEntryModel,
      event: HeightChanged
  ): Next<BMIEntryModel, BMIEntryEffect> {
    val updatedHeightModel = model.heightChanged(event.height)
    return if (event.height.length == 3) {
      next(updatedHeightModel, ChangeFocusToWeight)
    } else {
      next(updatedHeightModel)
    }
  }

  private fun onWeightBackSpaceClicked(
      model: BMIEntryModel
  ): Next<BMIEntryModel, BMIEntryEffect> {
    return if (model.weight.isNotEmpty()) {
      next(model.deleteWeightLastDigit())
    } else {
      val deleteWeightLastDigitModel = model.deleteWeightLastDigit()
      next(deleteWeightLastDigitModel, ChangeFocusToHeight)
    }
  }
}
