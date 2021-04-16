package org.simple.clinic.textInputdatepicker

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.PrefilledDate

class TextInputDatePickerInit : Init<TextInputDatePickerModel, TextInputDatePickerEffect> {
  override fun init(model: TextInputDatePickerModel): First<TextInputDatePickerModel, TextInputDatePickerEffect> {
    return First.first(model, setOf(PrefilledDate(model.prefilledDate)))
  }
}
