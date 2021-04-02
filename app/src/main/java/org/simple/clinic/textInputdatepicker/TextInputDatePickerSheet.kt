package org.simple.clinic.textInputdatepicker

import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.databinding.SheetTextInputDatePickerBinding
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet

class TextInputDatePickerSheet : BaseBottomSheet<
    TextInputDatePickerSheet.Key,
    SheetTextInputDatePickerBinding,
    TextInputDatePickerModel,
    TextInputDatePickerEvent,
    TextInputDatePickerEffect
    >(),
    TextInputDatePickerUiActions {

  override fun defaultModel() = TextInputDatePickerModel.create()

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) = SheetTextInputDatePickerBinding.inflate(inflater, container, false)

  @Parcelize
  object Key : ScreenKey() {

    override val analyticsName = "Text Input Date Picker"

    override fun instantiateFragment() = TextInputDatePickerSheet()

    override val type = ScreenType.Modal
  }
}
