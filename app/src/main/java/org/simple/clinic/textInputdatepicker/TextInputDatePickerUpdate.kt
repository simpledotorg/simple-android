package org.simple.clinic.textInputdatepicker

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.HideDateErrorMessage
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.DismissSheet

class TextInputDatePickerUpdate : Update<TextInputDatePickerModel, TextInputDatePickerEvent, TextInputDatePickerEffect> {
  override fun update(model: TextInputDatePickerModel, event: TextInputDatePickerEvent): Next<TextInputDatePickerModel, TextInputDatePickerEffect> {
    return when (event) {
      DismissSheetClicked -> dispatch(DismissSheet)
      is DayChanged -> next(model.dayChanged(event.day), HideDateErrorMessage)
    }
  }
}
